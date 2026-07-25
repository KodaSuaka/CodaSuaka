package com.example.codasuaka.data.repository

import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.NotificationDto
import com.example.codasuaka.data.remote.dto.PaginationMeta
import com.example.codasuaka.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotificationRepository {

    override suspend fun getNotifications(page: Int, perPage: Int): Result<Pair<List<NotificationDto>, PaginationMeta?>> {
        return try {
            val response = apiService.getNotifications(page, perPage)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Result.success(Pair(body.data, body.meta))
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal memuat notifikasi"))
                }
            } else {
                Result.failure(Exception("Gagal memuat notifikasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        return try {
            val response = apiService.getUnreadCount()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    Result.success(body.data.unreadCount)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal memuat jumlah notifikasi"))
                }
            } else {
                Result.failure(Exception("Gagal memuat jumlah notifikasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return try {
            val response = apiService.markNotificationRead(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menandai notifikasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menandai semua notifikasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: Int): Result<Unit> {
        return try {
            val response = apiService.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menghapus notifikasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}