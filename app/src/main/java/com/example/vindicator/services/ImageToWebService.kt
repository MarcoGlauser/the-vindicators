package com.example.vindicator.services

import android.content.ContentValues.TAG
import android.util.Log
import com.jayway.jsonpath.JsonPath
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*


class ImageToWebService {

    fun getInformationForImage(byteImage: ByteArray, lambda: (String) -> Unit) {

        val url = "https://api-beta.bite.ai"
        val token = "afa383c72087a4aea96567d1cb48ddfdcfef1679"


        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val original = chain.request()

                val request = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body())
                    .build()
                return chain.proceed(request)
            }
        })

        val client = httpClient.build()
        val retrofitClient =
            Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                .client(client).build()
        val visionCall = retrofitClient.create(VisionCall::class.java)

        val imageRequest = ImageRequest(encodeToBase64(byteImage))
        val requestCall = visionCall.createImageRequest(imageRequest)

        requestCall.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                var rawJson: String? = null
                try {
                    rawJson = response.body()?.string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                Log.d(TAG, rawJson)
                val name = JsonPath.read<String>(rawJson, "$.items[0].item.name")
                Log.d(TAG, name)
                lambda(name)

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, t.message)
                Log.d(TAG, t.toString())
                Log.d(TAG, "Fuck it")

            }
        })
    }

    private fun encodeToBase64(byteImage: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteImage)
    }

    companion object {

        private var single_instance: ImageToWebService? = null

        // static method to create instance of Singleton class
        val instance: ImageToWebService
            get() {
                if (single_instance == null)
                    single_instance = ImageToWebService()

                return single_instance as ImageToWebService
            }
    }

}

