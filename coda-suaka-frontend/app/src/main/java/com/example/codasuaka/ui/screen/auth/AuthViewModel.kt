package com.example.codasuaka.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
 *
 * ISSUE #3 (FIX): Sebelumnya langsung memanggil ApiService.getUser()
 * dari presentation layer, melanggar Clean Architecture.
 * Sekarang menggunakan AuthRepository (domain layer) untuk verifikasi.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    /**
     * Cek apakah token tersimpan masih valid.
     * 1. Inisialisasi cache token untuk AuthInterceptor
     * 2. Jika token tidak ada di DataStore → Unauthenticated
     * 3. Jika ada, verifikasi ke server via AuthRepository.verifyToken()
     *    - Sukses → Authenticated
     *    - Gagal (401) → Unauthenticated
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                // Init cache dulu agar AuthInterceptor bisa baca token
                tokenManager.initCache()

                val token = tokenManager.getToken()

                if (token.isNullOrBlank()) {
                    _authState.value = AuthState.Unauthenticated
                    return@launch
                }

                // Verifikasi token via repository (domain layer)
                val isValid = authRepository.verifyToken()
                if (isValid) {
                    _authState.value = AuthState.Authenticated
                } else {
                    // Token invalid/expired → bersihkan
                    tokenManager.clearAuthData()
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (_: Exception) {
                // Kalau gagal (misalnya network error), biarkan tetap Authenticated
                // agar user bisa buka offline data atau coba lagi nanti.
                val token = tokenManager.getCachedToken()
                _authState.value = if (!token.isNullOrBlank()) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Logout: hapus data auth, lalu ubah state ke Unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {
                // Abaikan error saat clear, tetap lanjut ubah state
            }
            _authState.value = AuthState.Unauthenticated
        }
    }
}
