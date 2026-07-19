package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("nama_instansi")
    val namaInstansi: String,

    @SerializedName("nama_pemilik")
    val namaPemilik: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
