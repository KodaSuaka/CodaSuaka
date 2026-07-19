package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: List<MessageDto>
)

data class SendMessageRequest(
    @SerializedName("penerima_id")
    val penerimaId: Int,

    @SerializedName("pesan")
    val pesan: String
)

data class SendMessageResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: MessageDto
)

data class MessageDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("pengirim_id")
    val pengirimId: Int = 0,

    @SerializedName("penerima_id")
    val penerimaId: Int = 0,

    @SerializedName("pesan")
    val pesan: String? = null,

    @SerializedName("is_read")
    val isRead: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("waktu")
    val waktu: String? = null
)
