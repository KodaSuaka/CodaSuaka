package com.example.codasuaka.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codasuaka.R
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
            is AuthState.Loading -> { /* tetap tampilkan splash */ }
        }
    }

    // ── Splash Screen dengan Logo ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo besar sebagai splash art
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "CodaSuaka Logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CodaSuaka",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Primary,
                strokeWidth = 3.dp
            )
        }
    }
}
