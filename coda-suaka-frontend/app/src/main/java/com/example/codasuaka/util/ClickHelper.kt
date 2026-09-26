package com.example.codasuaka.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * ClickHelper menyediakan utilitas untuk mencegah spam click (debouncing).
 *
 * Debounce dilacak per-key (default: "global"), bukan satu timestamp
 * bersama untuk seluruh aplikasi — supaya klik pada satu tombol tidak
 * ikut menahan klik pada tombol lain yang tidak terkait.
 */
object ClickHelper {
    private val lastClickTimes = mutableMapOf<String, Long>()
    private const val DEBOUNCE_TIME = 500L // 0.5 detik
    private const val GLOBAL_KEY = "global"

    /**
     * Mengecek apakah klik diperbolehkan berdasarkan interval waktu untuk [key].
     */
    fun canClick(key: String = GLOBAL_KEY): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastClickTime = lastClickTimes[key] ?: 0
        if (currentTime - lastClickTime < DEBOUNCE_TIME) {
            return false
        }
        lastClickTimes[key] = currentTime
        return true
    }
}

/**
 * Custom Modifier untuk menangani klik dengan proteksi anti-spam.
 * [key] membedakan debounce antar tombol; default memakai identitas onClick
 * lambda agar tombol yang berbeda tidak saling menahan klik satu sama lain.
 */
fun Modifier.safeClickable(
    enabled: Boolean = true,
    key: String? = null,
    onClick: () -> Unit
): Modifier = composed {
    val debounceKey = key ?: remember(onClick) { "safeClickable-${onClick.hashCode()}" }
    this.clickable(enabled = enabled) {
        if (ClickHelper.canClick(debounceKey)) {
            onClick()
        }
    }
}
