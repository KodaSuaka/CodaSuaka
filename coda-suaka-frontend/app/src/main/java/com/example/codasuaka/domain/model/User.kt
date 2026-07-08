package com.example.codasuaka.domain.model

/**
 * Domain model - representasi data user yang bersih.
 * Tidak tergantung pada API/DTO/Entity apapun.
 */
data class User(
    val id: Int,
    val email: String,
    val namaLengkap: String,
    val role: String,
    val instansiId: String?,
    val outletId: String?,
    val token: String,
    val permissions: List<String> = emptyList()
)
