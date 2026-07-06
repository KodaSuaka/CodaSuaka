package com.example.codasuaka.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatContactResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: List<ContactGroupDto>
)

data class ContactGroupDto(
    @SerializedName("role")
    val role: String,

    @SerializedName("contacts")
    val contacts: List<ContactDto>
)

data class ContactDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("role_id")
    val roleId: Int,

    @SerializedName("nama_lengkap")
    val namaLengkap: String,

    @SerializedName("foto_profil")
    val fotoProfil: String?,

    @SerializedName("unread_count")
    val unreadCount: Int,

    @SerializedName("last_message")
    val lastMessage: String?,

    @SerializedName("last_message_time")
    val lastMessageTime: String?
)
