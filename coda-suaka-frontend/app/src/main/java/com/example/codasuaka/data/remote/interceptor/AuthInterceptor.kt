package com.example.codasuaka.data.remote.interceptor

import com.example.codasuaka.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor untuk menyisipkan token Bearer ke setiap request API.
 * Menggunakan cachedToken dari TokenManager agar tidak perlu runBlocking
 * (OkHttp Interceptor tidak bisa menjalankan coroutine).
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getCachedToken()
        val hasToken = !token.isNullOrBlank()

        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .apply {
                if (hasToken) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        val response = chain.proceed(request)

        // Hanya anggap sesi habis jika request ini memang membawa token
        // (bukan 401 dari login/register yang gagal karena kredensial salah).
        if (hasToken && response.code == 401) {
            tokenManager.clearTokenAndNotifyExpired()
        }

        return response
    }
}
