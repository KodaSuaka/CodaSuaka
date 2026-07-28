package com.example.codasuaka.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Menjalankan [onResume] setiap kali layar ini kembali ke foreground —
 * baik dari background (app di-minimize) maupun dari back-stack (navigasi
 * balik ke layar ini). Dipakai supaya data seperti daftar tugas tidak basi
 * setelah pergantian hari/shift sambil app tetap terbuka.
 */
@Composable
fun OnResumeEffect(onResume: () -> Unit) {
    val currentOnResume = rememberUpdatedState(onResume)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentOnResume.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
