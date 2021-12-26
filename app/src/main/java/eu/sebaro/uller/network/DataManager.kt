package eu.sebaro.uller.network

import androidx.compose.runtime.mutableStateListOf
import eu.sebaro.uller.BuildConfig
import eu.sebaro.uller.ScrapItems
import eu.sebaro.uller.StatusDTO
import eu.sebaro.uller.log
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody

class DataManager {
    private val favoriteProducts = mutableStateListOf<String>()

    private val json = Json
    private val restClient = KRestApi(BuildConfig.HOST_URL, json) {
        addHeader("Api-Version", "${BuildConfig.API_VERSION}")
        addHeader("App-Version", BuildConfig.APP_VERSION)
    }

    suspend fun requestProductList(): ScrapItems {
        log("Get device config from server")
        return restClient.get().path("productlist").execute()
    }

    suspend fun handleSubscription(id: String, token: String, subscribe: Boolean): StatusDTO {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        builder.addFormDataPart("productId", id)
        builder.addFormDataPart("token", token)
        builder.addFormDataPart("subscribe", subscribe.toString())
        val requestBody = builder.build()
        return restClient.post(requestBody).path("subscription").execute()
    }

    fun unSubscribe(productName: String) {
        favoriteProducts.remove(productName)
    }

    fun subscribe(productName: String) {
        favoriteProducts.add(productName)
    }

    fun isSubscribed(productName: String) = favoriteProducts.contains(productName)
}
