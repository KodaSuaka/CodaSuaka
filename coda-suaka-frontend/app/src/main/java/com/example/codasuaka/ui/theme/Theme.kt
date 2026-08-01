package com.example.codasuaka.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Tertiary,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    onError = OnPrimary,
    error = Error
)

@Composable
fun CodaSuakaTheme(
    darkTheme: Boolean = false, // Dipaksa Light Mode sesuai keinginan user
    content: @Composable () -> Unit
) {
    // Aplikasi selalu Light Mode — DarkColorScheme sudah dihapus.
    // Param darkTheme dipertahankan hanya demi kompatibilitas pemanggil.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
