package com.example.codasuaka.ui.screen.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.R
import com.example.codasuaka.ui.theme.Primary
import com.example.codasuaka.ui.theme.Tertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: (role: String) -> Unit,
    onUnauthenticated: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    // ── Animation States ──
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        // Menjalankan animasi secara paralel
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        
        // Sedikit delay agar user sempat menikmati animasi sebelum pindah layar
        delay(1200)

        // Efek navigasi berdasarkan state autentikasi
        when (val state = authState) {
            is AuthState.Authenticated -> onAuthenticated(state.role)
            is AuthState.Unauthenticated -> onUnauthenticated()
            is AuthState.Loading -> { /* Tunggu loading selesai jika belum */ }
        }
    }

    // Memantau jika authState berubah setelah animasi selesai
    LaunchedEffect(authState) {
        if (alpha.value == 1f) { // Jika animasi sudah selesai/berjalan
            when (val state = authState) {
                is AuthState.Authenticated -> onAuthenticated(state.role)
                is AuthState.Unauthenticated -> onUnauthenticated()
                else -> {}
            }
        }
    }

    // ── Animated Splash Screen ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Logo besar dengan animasi
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "CodaSuaka Logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CodaSuaka",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mengembalikan loading indicator namun tetap mengikuti animasi memudar
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Primary,
                strokeWidth = 3.dp
            )
        }
    }
}
