package com.example.codasuaka.di

import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.util.BluetoothPrinterManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Module Koin untuk dependency umum aplikasi.
 * Menyediakan TokenManager, dan dependency global lainnya.
 */
val appModule = module {
    single { TokenManager(androidContext()) }
    single { com.example.codasuaka.data.local.PreferenceManager(androidContext()) }
    single { BluetoothPrinterManager(androidContext(), get<com.example.codasuaka.data.local.PreferenceManager>()) }
}
