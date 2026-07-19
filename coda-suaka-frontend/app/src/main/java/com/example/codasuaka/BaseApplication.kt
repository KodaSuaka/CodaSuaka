package com.example.codasuaka

import android.app.Application
import com.example.codasuaka.di.appModule
import com.example.codasuaka.di.dataModule
import com.example.codasuaka.di.domainModule
import com.example.codasuaka.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * BaseApplication untuk inisialisasi Koin DI.
 * Sesuai modul: di/BaseApplication.kt
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    appModule,
                    dataModule,
                    domainModule,
                    viewModelModule
                )
            )
        }
    }
}
