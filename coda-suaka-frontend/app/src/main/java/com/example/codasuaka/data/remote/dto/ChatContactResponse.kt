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
    val name: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("role_id")
    val roleId: Int = 0,

    @SerializedName("nama_lengkap")
    val namaLengkap: String? = null,

    @SerializedName("foto_profil")
    val fotoProfil: String? = null,

    @SerializedName("unread_count")
    val unreadCount: Int = 0,

    @SerializedName("last_message")
    val lastMessage: String? = null,

    @SerializedName("last_message_time")
    val lastMessageTime: String? = null
)
