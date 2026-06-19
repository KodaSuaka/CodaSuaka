package com.example.codasuaka.data.remote

import com.example.codasuaka.data.remote.dto.LoginRequest
import com.example.codasuaka.data.remote.dto.LoginResponse
import com.example.codasuaka.data.remote.dto.RegisterRequest
import com.example.codasuaka.data.remote.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
