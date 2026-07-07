package com.example.codasuaka.ui.screen.auth

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
        checkAuthStatus()
    }

    /**
     * Cek apakah token tersimpan masih ada.
     * Jika ada, arahkan ke Dashboard (Authenticated).
     * Jika tidak ada, arahkan ke Login (Unauthenticated).
     *
     * PENTING: fungsi ini HANYA membaca token, tidak pernah menghapusnya.
     * Penghapusan token hanya boleh terjadi lewat logout() eksplisit.
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first()
                _authState.value = if (!token.isNullOrBlank()) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                // Kalau gagal baca token karena alasan apapun, anggap belum login
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Logout: hapus data auth, lalu ubah state ke Unauthenticated.
     * Ini satu-satunya tempat yang boleh menghapus token.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                tokenManager.clearAuthData()
            } catch (_: Exception) {
                // Abaikan error saat clear, tetap lanjut ubah state
            }
            _authState.value = AuthState.Unauthenticated
        }
    }
}