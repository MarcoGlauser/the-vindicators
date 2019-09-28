package com.example.vindicator.services

import java.io.Serializable

class ImageRequest : Serializable{
    private var base64: String? = null

    internal constructor() {

    }

    internal constructor(file: String) {
        this.base64 = "data:image/jpeg;base64,$file"
    }

    fun getBase64(): String? {
        return this.base64
    }

    fun setBase64(file: String) {
        this.base64 = "data:image/jpeg;base64,$file"
    }

}
