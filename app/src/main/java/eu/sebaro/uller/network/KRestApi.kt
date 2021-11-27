package eu.sebaro.uller.network

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.nio.charset.Charset

data class PinnedDomain(val domain: String, val certificateFingerPrint: String)

class HttpException(val code: Int, override val message: String, val body: String?) : IOException(message)
class JsonParsingError(): IOException()

class KRestApi(
    private val baseUrl: String,
    val json: Json,
    private val config: ConfigScope.() -> Unit = {}
) {
    private var errorHandler: (Throwable) -> Throwable = { it }

    /**
     * creates a client with configured logging and header interceptors
     */
    private val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
            val scope = ConfigScope(builder)
            config(scope)
            errorHandler = scope.errorHandler
            createLoggingInterceptor(builder)
            return builder.build()
        }

    fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket = client.newWebSocket(request, listener)

    fun get() = MethodScope(Request.Builder().get())

    fun post(body: RequestBody) = MethodScope(Request.Builder().post(body))

    inline fun <reified T : Any> post(body: T): MethodScope =
        post(json.encodeToString(body).toRequestBody("application/json".toMediaType()))

    fun put(body: RequestBody) = MethodScope(Request.Builder().put(body))

    inline fun <reified T : Any> put(body: T): MethodScope =
        put(json.encodeToString(body).toRequestBody("application/json".toMediaType()))

    fun patch(body: RequestBody) = MethodScope(Request.Builder().patch(body))

    inline fun <reified T : Any> patch(body: T): MethodScope =
        patch(json.encodeToString(body).toRequestBody("application/json".toMediaType()))

    inner class MethodScope(private val requestBuilder: Request.Builder) {
        fun path(path: String): UrlScope = UrlScope(json, requestBuilder, baseUrl.toHttpUrl().newBuilder().addPathSegments(path.trimStart('/')))

        fun url(url: String) = UrlScope(json, requestBuilder, url.toHttpUrl().newBuilder()) // absolute URL

        fun addHeader(name: String, value: String?): MethodScope {
            if (value != null) {
                requestBuilder.addHeader(name, value)
            }
            return this
        }
    }

    private fun Call.executeWithErrorHandler() {
        executeWithErrorHandler { }
    }
    private inline fun <R> Call.executeWithErrorHandler(block: (Response) -> R): R = try {
        execute().use { response ->
            checkResponse(response)
            block(response)
        }
    } catch (err: Throwable) {
        log(err)
        throw errorHandler(err)
    }

    inner class UrlScope(val json: Json, private val requestBuilder: Request.Builder, private val urlBuilder: HttpUrl.Builder) {
        suspend fun executeNoResponseExpected() {
            withContext(Dispatchers.IO) {
                requestBuilder.url(urlBuilder.build())
                client.newCall(requestBuilder.build()).executeWithErrorHandler()
            }
        }

        suspend fun <T : Any> executeList(serializer: KSerializer<T>): List<T> {
            val listSerializer = ListSerializer(serializer)
            return execute(listSerializer)
        }

        suspend inline fun <reified T : Any> execute(): T = execute(json.serializersModule.serializer<T>())
        suspend fun <T : Any> execute(serializer: KSerializer<T>): T = withContext(Dispatchers.IO) {
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Accept-Charset", "UTF-8")
            requestBuilder.url(urlBuilder.build())
            client.newCall(requestBuilder.build()).executeWithErrorHandler { response ->
                response.body?.use { body ->
                    return@withContext json.decodeFromString(serializer, body.source().readString(Charset.forName("utf8")))
                }
            }
            throw IOException("No body!")
        }
        suspend inline fun <reified T : Any> executeNA(): T? = executeNA(json.serializersModule.serializer<T>())
        suspend fun <T : Any> executeNA(serializer: KSerializer<T>): T? = withContext(Dispatchers.IO) {
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Accept-Charset", "UTF-8")
            requestBuilder.url(urlBuilder.build())
            client.newCall(requestBuilder.build()).executeWithErrorHandler { response ->
                response.body?.use { body ->
                    if (body.source().exhausted()) {
                        return@withContext null
                    } else {
                        return@withContext json.decodeFromString(serializer, body.source().readString(Charset.forName("utf8")))
                    }
                }
            }
            null
        }

        fun queryParam(name: String, value: String?): UrlScope {
            if (value != null) { // do not add if null
                urlBuilder.addQueryParameter(name, value)
            }
            return this
        }
    }

    private fun checkResponse(response: Response) {
        when (val code = response.code) {
            in 200..300 -> return
            else -> throw HttpException(code, response.message, response.body?.string())
        }
    }

    private fun createLoggingInterceptor(client: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor { message -> Log.v("OkHttpClient", message) }
            logging.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logging)
        }
    }
}

class ConfigScope(val builder: OkHttpClient.Builder) {
    private val headersMap = mutableMapOf<String, String>()
    var errorHandler: (Throwable) -> Throwable = { it }

    private val headerInterceptor: Interceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            with(original.newBuilder()) {
                headersMap.forEach { (name, value) ->
                    addHeader(name, value)
                }
                return chain.proceed(build())
            }
        }
    }

    fun addHeader(name: String, value: String?) {
        if (headersMap.isEmpty()) { // first use
            addInterceptor(headerInterceptor)
        }

        value?.let {
            headersMap[name] = it
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        builder.addInterceptor(interceptor)
    }

    fun addBasicAuth(user: String, password: String) {
        val authHeaderKey = "Authorization"

        val userAndPassword = "$user:$password"
        val encoded = Base64.encodeToString(userAndPassword.encodeToByteArray(), Base64.NO_WRAP)
        val credentials = "Basic $encoded"

        addHeader(authHeaderKey, credentials)
    }

    fun addPinnedDomains(pinnedDomainList: List<PinnedDomain>) {
        val certificatePinner = CertificatePinner.Builder().apply {
            pinnedDomainList.forEach {
                add(it.domain, it.certificateFingerPrint)
            }
        }.build()
        builder.certificatePinner(certificatePinner)
    }
}
