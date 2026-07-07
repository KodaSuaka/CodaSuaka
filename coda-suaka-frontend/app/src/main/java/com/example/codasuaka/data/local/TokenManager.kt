package com.example.codasuaka.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    /**
     * Sinkronkan cache token agar AuthInterceptor bisa membaca token
     * tanpa perlu runBlocking. Cache ini diupdate setiap kali
     * saveAuthData() atau clearAuthData() dipanggil.
     */
    @Volatile
    private var cachedToken: String? = null

    /**
     * Mengembalikan token yang terakhir di-cache secara sinkron.
     * Cocok untuk OkHttp Interceptor yang tidak bisa pakai coroutine.
     */
    fun getCachedToken(): String? = cachedToken

    /**
     * Memuat token dari DataStore ke cache.
     * Harus dipanggil di awal aplikasi (misalnya dari AuthViewModel)
     * agar AuthInterceptor bisa membaca token tanpa runBlocking.
     */
    suspend fun initCache() {
        cachedToken = context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }.first()
    }

    suspend fun saveAuthData(
        token: String,
        email: String,
        name: String,
        role: String,
        userId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_NAME_KEY] = name
            prefs[USER_ROLE_KEY] = role
            prefs[USER_ID_KEY] = userId
        }
        cachedToken = token // update cache
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ROLE_KEY]
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        cachedToken = null // clear cache
    }
}
