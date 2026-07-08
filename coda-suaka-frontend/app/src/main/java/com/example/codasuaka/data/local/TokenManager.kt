package com.example.codasuaka.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manajer penyimpanan token dan data pengguna terenkripsi.
 *
 * Menggunakan [EncryptedSharedPreferences] dari AndroidX Security Crypto
 * untuk menjamin data auth (token, email, nama, role, user ID) tersimpan
 * dalam keadaan terenkripsi — baik saat at-rest maupun pada level file.
 *
 * ISSUE #2 (FIX): Sebelumnya menggunakan DataStore Preferences plaintext,
 * yang menyebabkan token JWT dan data pengguna tersimpan tanpa enkripsi.
 */
class TokenManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val PREFS_FILE_NAME = "codasuaka_secure_auth_prefs"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
    }

    /**
     * Cache in-memory agar [AuthInterceptor] bisa membaca token secara
     * sinkron tanpa harus membuka EncryptedSharedPreferences setiap kali.
     */
    @Volatile
    private var cachedToken: String? = null

    /** Mengembalikan token dari cache — cocok untuk OkHttp Interceptor. */
    fun getCachedToken(): String? = cachedToken

    /**
     * Memuat token dari penyimpanan terenkripsi ke cache in-memory.
     * Harus dipanggil sekali di awal aplikasi (misalnya dari splash / AuthViewModel).
     */
    suspend fun initCache() {
        cachedToken = prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Menyimpan data autentikasi ke EncryptedSharedPreferences.
     * Semua nilai dienkripsi sebelum ditulis ke file XML.
     */
    suspend fun saveAuthData(
        token: String,
        email: String,
        name: String,
        role: String,
        userId: String
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .apply()
        cachedToken = token
    }

    /**
     * Mengembalikan token dari penyimpanan terenkripsi (synchronous).
     * Digunakan oleh Flow / coroutine yang memanggil .first().
     */
    suspend fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /**
     * Mengembalikan email user dari penyimpanan terenkripsi (synchronous).
     */
    suspend fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    /**
     * Mengembalikan nama user dari penyimpanan terenkripsi (synchronous).
     */
    suspend fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    /**
     * Mengembalikan role user dari penyimpanan terenkripsi (synchronous).
     */
    suspend fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    /**
     * Menghapus seluruh data autentikasi (saat logout).
     */
    suspend fun clearAuthData() {
        prefs.edit().clear().apply()
        cachedToken = null
    }
}
