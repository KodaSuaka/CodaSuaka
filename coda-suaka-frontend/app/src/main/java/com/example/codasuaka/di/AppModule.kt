package com.example.codasuaka.di

import com.example.codasuaka.data.local.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module Koin untuk dependency umum aplikasi.
 * Menyediakan TokenManager, dan dependency global lainnya.
 */
val appModule = module {
    single { TokenManager(androidContext()) }
}
