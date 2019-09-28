package com.example.vindicator.services

import java.util.*


class ImageToWebService {

    fun getInformationForImage(byteImage: ByteArray): String {

        return "Banana"
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
