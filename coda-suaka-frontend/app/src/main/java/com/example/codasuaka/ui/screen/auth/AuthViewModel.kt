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
        // Langsung navigasi ke Login — tidak ada auto-login ke Dashboard
        // agar tidak memicu error dari panggilan API dashboard saat token tidak valid.
        clearTokenAndGoToLogin()
    }

    /**
     * Hapus token tersimpan (jika ada) lalu navigasi ke halaman Login.
     * Dengan demikian splash screen akan selalu menuju ke Login,
     * bukan langsung ke Dashboard yang bisa memicu error.
     */
    private fun clearTokenAndGoToLogin() {
        viewModelScope.launch {
            try {
                tokenManager.clearAuthData()
            } catch (_: Exception) {
                // Abaikan error — yang penting state berubah ke Unauthenticated
            }
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Logout: hapus data auth, lalu ubah state ke Unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                tokenManager.clearAuthData()
            } catch (_: Exception) { }
            _authState.value = AuthState.Unauthenticated
        }
    }
}
