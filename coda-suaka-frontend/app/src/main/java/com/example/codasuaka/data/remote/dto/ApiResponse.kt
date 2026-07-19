package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String
)
