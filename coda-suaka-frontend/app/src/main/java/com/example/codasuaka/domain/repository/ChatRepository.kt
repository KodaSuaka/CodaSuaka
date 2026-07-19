package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.*

interface ChatRepository {
    suspend fun getContacts(): Result<List<ContactGroupDto>>
    suspend fun getMessages(userId: Int): Result<List<MessageDto>>
    suspend fun sendMessage(penerimaId: Int, pesan: String): Result<MessageDto>
    suspend fun markAsRead(userId: Int): Result<Unit>
}
