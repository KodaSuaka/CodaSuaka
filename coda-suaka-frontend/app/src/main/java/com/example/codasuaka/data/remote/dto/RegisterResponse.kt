package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RegisterData?
)

data class RegisterData(
    @SerializedName("user")
    val user: UserData?,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("permissions")
    val permissions: List<String>?
)
