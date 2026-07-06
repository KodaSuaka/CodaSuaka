package com.example.codasuaka.data.remote

import com.example.codasuaka.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // ─── Chat / Kontak ─────────────────────────────────────

    @GET("chat/contacts")
    suspend fun getChatContacts(): Response<ChatContactResponse>

    @GET("chat/messages/{userId}")
    suspend fun getChatMessages(
        @Path("userId") userId: Int
    ): Response<ChatMessageResponse>

    @POST("chat/send")
    suspend fun sendChatMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @PUT("chat/read/{userId}")
    suspend fun markChatRead(
        @Path("userId") userId: Int
    ): Response<Unit>
}
