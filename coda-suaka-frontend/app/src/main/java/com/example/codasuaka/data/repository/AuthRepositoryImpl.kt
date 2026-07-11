package com.example.codasuaka.data.repository

import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.dto.LoginRequest
import com.example.codasuaka.data.remote.dto.RegisterRequest
import com.example.codasuaka.domain.model.User
import com.example.codasuaka.domain.repository.AuthRepository

/**
 * Implementasi dari AuthRepository (domain interface).
 * Data layer menerjemahkan DTO ke Domain Model.
 */
class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userDto = body.data?.user
                val token = body.data?.accessToken ?: ""
                val permissions = body.data?.permissions ?: emptyList()
                val user = User(
                    id = userDto?.id ?: 0,
                    email = userDto?.email ?: email,
                    namaLengkap = userDto?.namaLengkap ?: "",
                    role = userDto?.role ?: "",
                    instansiId = userDto?.instansiId,
                    outletId = userDto?.outletId,
                    token = token,
                    permissions = permissions
                )
                // Hapus data auth lama sebelum menyimpan yang baru
                // (mencegah conflict token antar user di device yang sama)
                tokenManager.clearAuthData()
                // Simpan token
                tokenManager.saveAuthData(
                    token = token,
                    email = user.email,
                    name = user.namaLengkap,
                    role = user.role,
                    userId = user.id.toString(),
                    permissions = permissions
                )
                Result.success(user)
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    override suspend fun register(
        namaInstansi: String,
        namaPemilik: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val response = apiService.register(
                RegisterRequest(namaInstansi, namaPemilik, email, password)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body.data?.accessToken ?: ""
                // Ekstrak userId dari response user (Gson parse sebagai LinkedTreeMap)
                val userId = (body.data?.user as? Map<*, *>)?.get("id")?.toString() ?: ""
                val user = User(
                    id = userId.toIntOrNull() ?: 0,
                    email = email,
                    namaLengkap = namaPemilik,
                    role = "Owner",
                    instansiId = null,
                    outletId = null,
                    token = token
                )
                // Hapus data auth lama sebelum menyimpan yang baru
                // (mencegah conflict token antar user di device yang sama)
                tokenManager.clearAuthData()
                tokenManager.saveAuthData(
                    token = token,
                    email = email,
                    name = namaPemilik,
                    role = "Owner",
                    userId = userId
                )
                Result.success(user)
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun verifyToken(): Boolean {
        return try {
            val response = apiService.getUser()
            response.isSuccessful
        } catch (_: Exception) {
            // Network error — tidak bisa diverifikasi, anggap valid
            // agar user tetap bisa akses offline data
            getToken() != null
        }
    }

    override suspend fun logout() {
        tokenManager.clearAuthData()
    }

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        return when (code) {
            401 -> "Email atau Password yang Anda masukkan salah."
            422 -> {
                try {
                    val json = org.json.JSONObject(errorBody ?: "{}")
                    val errors = json.optJSONObject("errors")
                    if (errors != null) {
                        val messages = mutableListOf<String>()
                        errors.keys().forEach { key ->
                            val arr = errors.getJSONArray(key)
                            messages.add(arr.getString(0))
                        }
                        messages.joinToString("\n")
                    } else {
                        json.optString("message", "Validasi gagal.")
                    }
                } catch (e: Exception) {
                    "Validasi gagal. Periksa data yang Anda masukkan."
                }
            }
            500 -> "Terjadi kesalahan server. Silakan coba lagi nanti."
            else -> "Terjadi kesalahan. Kode error: $code"
        }
    }
}
