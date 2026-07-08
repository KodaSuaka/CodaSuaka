package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoginData?
)

data class LoginData(
    @SerializedName("user")
    val user: UserData,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("permissions")
    val permissions: List<String>?
)

data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("instansi_id")
    val instansiId: String?,

    @SerializedName("outlet_id")
    val outletId: String?,

    @SerializedName("nama_lengkap")
    val namaLengkap: String
)
