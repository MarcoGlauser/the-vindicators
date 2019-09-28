package com.example.vindicator.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VisionCall {

    @POST("/vision/")
    fun createImageRequest(@Body imageRequest: ImageRequest): Call<ResponseBody>

}
