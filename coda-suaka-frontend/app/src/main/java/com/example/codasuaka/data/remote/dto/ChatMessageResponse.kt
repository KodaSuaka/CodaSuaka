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
    val id: Int,

    @SerializedName("pengirim_id")
    val pengirimId: Int,

    @SerializedName("penerima_id")
    val penerimaId: Int,

    @SerializedName("pesan")
    val pesan: String,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("waktu")
    val waktu: String
)
