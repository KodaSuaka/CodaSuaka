package com.example.codasuaka.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Status autentikasi aplikasi.
 */
sealed class AuthState {
    /** Masih memeriksa token — tampilkan loading splash */
    data object Loading : AuthState()

    /** Token valid — langsung navigasi ke dashboard */
    data object Authenticated : AuthState()

    /** Token tidak ada / expired — navigasi ke login */
    data object Unauthenticated : AuthState()
}

/**
 * ViewModel untuk mengecek status autentikasi awal aplikasi.
 * Digunakan oleh AuthScreen sebagai gatekeeper sebelum masuk ke
 * Dashboard atau Login.
 */
class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuth()
    }

    /**
     * Mengecek apakah token tersimpan dan valid.
     * Jika token ada → Authenticated (ke Dashboard)
     * Jika token tidak ada → Unauthenticated (ke Login)
     */
    private fun checkAuth() {
        viewModelScope.launch {
            val token = tokenManager.token.first()
            _authState.value = if (token.isNullOrEmpty()) {
                AuthState.Unauthenticated
            } else {
                // TODO: Opsional — validasi token ke server (cek expiry)
                AuthState.Authenticated
            }
        }
    }

    /**
     * Logout: hapus data auth, lalu ubah state ke Unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAuthData()
            _authState.value = AuthState.Unauthenticated
        }
    }
}
