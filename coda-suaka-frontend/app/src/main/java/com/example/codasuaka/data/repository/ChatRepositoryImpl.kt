package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.*
import com.example.codasuaka.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val apiService: ApiService
) : ChatRepository {

    override suspend fun getContacts(): Result<List<ContactGroupDto>> {
        return try {
            val response = apiService.getChatContacts()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Gagal memuat kontak"))
                }
            } else {
                Result.failure(Exception("Gagal memuat kontak: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(userId: Int): Result<List<MessageDto>> {
        return try {
            val response = apiService.getChatMessages(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Gagal memuat pesan"))
                }
            } else {
                Result.failure(Exception("Gagal memuat pesan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(penerimaId: Int, pesan: String): Result<MessageDto> {
        return try {
            val response = apiService.sendChatMessage(
                SendMessageRequest(penerimaId = penerimaId, pesan = pesan)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Gagal mengirim pesan"))
                }
            } else {
                Result.failure(Exception("Gagal mengirim pesan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(userId: Int): Result<Unit> {
        return try {
            val response = apiService.markChatRead(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menandai pesan sudah dibaca"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
