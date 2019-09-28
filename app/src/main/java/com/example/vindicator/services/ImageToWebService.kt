package com.example.vindicator.services

import android.content.ContentValues.TAG
import android.util.Log
import com.jayway.jsonpath.JsonPath
import ch.viascom.groundwork.foxhttp.exception.FoxHttpException
import ch.viascom.groundwork.foxhttp.body.request.RequestObjectBody
import ch.viascom.groundwork.foxhttp.header.HeaderEntry
import ch.viascom.groundwork.foxhttp.type.RequestType
import ch.viascom.groundwork.foxhttp.builder.FoxHttpRequestBuilder
import ch.viascom.groundwork.foxhttp.FoxHttpResponse
import ch.viascom.groundwork.foxhttp.interceptor.response.HttpErrorResponseInterceptor
import ch.viascom.groundwork.foxhttp.interceptor.FoxHttpInterceptorType
import ch.viascom.groundwork.foxhttp.parser.GsonParser
import ch.viascom.groundwork.foxhttp.builder.FoxHttpClientBuilder
import ch.viascom.groundwork.foxhttp.FoxHttpClient
import java.io.IOException
import java.util.*


class ImageToWebService {

    fun getInformationForImage(byteImage: ByteArray): String {

        var foxHttpClient: FoxHttpClient? = null
        try {
            foxHttpClient = FoxHttpClientBuilder(GsonParser()).addFoxHttpInterceptor(
                FoxHttpInterceptorType.RESPONSE,
                HttpErrorResponseInterceptor()
            ).build()
        } catch (e: FoxHttpException) {
            e.printStackTrace()
        }

        // Define a System-Out logger on DEBUG level
        // foxHttpClient.setFoxHttpLogger(new SystemOutFoxHttpLogger(true, "FoxHttp-Logger", FoxHttpLoggerLevel.DEBUG));

        // Create and Execute GET Request
        val url = "https://api-beta.bite.ai/vision/"
        val token = "afa383c72087a4aea96567d1cb48ddfdcfef1679"
        var response: FoxHttpResponse? = null
        try {
            response = FoxHttpRequestBuilder(url, RequestType.POST, foxHttpClient).addRequestHeader(
                HeaderEntry(
                    "Authorization",
                    "Bearer $token"
                )
            )
                .setRequestBody(RequestObjectBody(ImageRequest(encodeToBase64(byteImage))))
                .buildAndExecute()
        } catch (e: FoxHttpException) {
            e.printStackTrace()
        }

        // Deserialization response
        var rawJson: String? = null
        try {
            rawJson = response!!.stringBody
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d(TAG,rawJson)
        val name = JsonPath.read<String>(rawJson, "$.items[0].item.name")
        Log.d(TAG, name)

        return name
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
