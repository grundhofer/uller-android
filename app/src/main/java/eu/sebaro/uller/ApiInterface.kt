package eu.sebaro.uller

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET


interface ApiInterface {

    @GET("/productlist")
    fun getMovies() : Call<List<GeneralDataModels.ProductListDto>>

    companion object {
        var BASE_URL = "http://0.0.0.0:8080"
        private val contentType =  "application/json"
        private val jsonConfig = JsonConfiguration.Stable.copy(prettyPrint = true, ignoreUnknownKeys = true)

    private val json = Json(jsonConfig)

        fun create() : ApiInterface {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(json.asConverterFactory(contentType.toMediaType()))
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiInterface::class.java)

        }
    }
}





