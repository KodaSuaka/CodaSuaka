package com.example.codasuaka.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * ClickHelper menyediakan utilitas untuk mencegah spam click (debouncing).
 */
object ClickHelper {
    private var lastClickTime: Long = 0
    private const val DEBOUNCE_TIME = 500L // 0.5 detik

    /**
     * Mengecek apakah klik diperbolehkan berdasarkan interval waktu.
     */
    fun canClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < DEBOUNCE_TIME) {
            return false
        }
        lastClickTime = currentTime
        return true
    }
}

/**
 * Custom Modifier untuk menangani klik dengan proteksi anti-spam.
 */
fun Modifier.safeClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(enabled = enabled) {
        if (ClickHelper.canClick()) {
            onClick()
        }
    }
}
