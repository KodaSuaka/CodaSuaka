package com.example.codasuaka.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codasuaka.ui.theme.Primary
import com.example.codasuaka.ui.theme.Tertiary

/**
 * AuthScreen — Gerbang autentikasi aplikasi.
 *
 * Screen ini bertugas mengecek apakah user sudah login (token tersimpan)
 * atau belum. Selama proses pengecekan, ditampilkan loading splash.
 *
 * Setelah status diketahui:
 * - [AuthState.Authenticated]   → panggil [onAuthenticated] → navigasi ke Dashboard
 * - [AuthState.Unauthenticated] → panggil [onUnauthenticated] → navigasi ke Login
 *
 * @param viewModel  AuthViewModel yang mengelola state autentikasi
 * @param onAuthenticated   Callback saat user terautentikasi
 * @param onUnauthenticated Callback saat user belum login
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    val authState = viewModel.authState

    // ── Efek navigasi berdasarkan state autentikasi ──
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> onAuthenticated()
            is AuthState.Unauthenticated -> onUnauthenticated()
            is AuthState.Loading -> { /* tetap tampilkan loading */ }
        }
    }

    // ── Tampilan loading splash ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Primary,
            strokeWidth = 4.dp
        )
    }
}
