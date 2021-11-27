package eu.sebaro.uller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiInterface = ApiInterface.create().getMovies()

//        apiInterface.enqueue( Callback<List<ProductListDto>>())
        apiInterface.enqueue( object : Callback<List<GeneralDataModels.ProductListDto>> {
            override fun onResponse(call: Call<List<GeneralDataModels.ProductListDto>>?, response: Response<List<GeneralDataModels.ProductListDto>>?) {

                if(response?.body() != null)
                    log("${response.body()}")
            }

            override fun onFailure(call: Call<List<GeneralDataModels.ProductListDto>>?, t: Throwable?) {

            }
        })
    }
}