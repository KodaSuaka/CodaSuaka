package com.example.codasuaka.domain.repository

import com.example.codasuaka.domain.model.User

/**
 * Interface repository untuk autentikasi.
 * Domain layer hanya tahu interface ini, bukan implementasinya.
 */
interface AuthRepository {

    /** Login dengan email dan password */
    suspend fun login(email: String, password: String): Result<User>

    /** Register instansi baru */
    suspend fun register(
        namaInstansi: String,
        namaPemilik: String,
        email: String,
        password: String
    ): Result<User>

    /** Ambil token yang tersimpan */
    suspend fun getToken(): String?

    /** Hapus data auth (logout) */
    suspend fun logout()
}
