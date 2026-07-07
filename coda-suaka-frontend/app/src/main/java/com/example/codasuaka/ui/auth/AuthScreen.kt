package com.example.codasuaka.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codasuaka.ui.theme.Primary
import com.example.codasuaka.ui.theme.Tertiary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    // ── Efek navigasi berdasarkan state autentikasi ──
    LaunchedEffect(authState) {
        when (authState) {
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
