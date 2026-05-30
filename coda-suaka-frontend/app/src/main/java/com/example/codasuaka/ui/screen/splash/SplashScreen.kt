package com.example.codasuaka.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.codasuaka.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigationToLogin: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000L) // Delay for 2 seconds
        onNavigationToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_splash),
            contentDescription = "Coda Suaka Splash Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
