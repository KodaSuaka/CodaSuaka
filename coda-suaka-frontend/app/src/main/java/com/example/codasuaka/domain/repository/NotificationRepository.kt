package com.example.codasuaka.domain.repository

import com.example.codasuaka.data.remote.dto.NotificationDto
import com.example.codasuaka.data.remote.dto.PaginationMeta

interface NotificationRepository {
    /**
     * Ambil daftar notifikasi (paginated).
     */
    suspend fun getNotifications(page: Int = 1, perPage: Int = 20): Result<Pair<List<NotificationDto>, PaginationMeta?>>

    /**
     * Ambil jumlah notifikasi belum dibaca.
     */
    suspend fun getUnreadCount(): Result<Int>

    /**
     * Tandai satu notifikasi sudah dibaca.
     */
    suspend fun markAsRead(notificationId: Int): Result<Unit>

    /**
     * Tandai semua notifikasi sudah dibaca.
     */
    suspend fun markAllAsRead(): Result<Unit>

    /**
     * Hapus notifikasi.
     */
    suspend fun deleteNotification(notificationId: Int): Result<Unit>
}