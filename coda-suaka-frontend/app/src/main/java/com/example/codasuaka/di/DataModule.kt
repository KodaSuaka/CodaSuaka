package com.example.codasuaka.di

import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.interceptor.AuthInterceptor
import com.example.codasuaka.data.repository.*
import com.example.codasuaka.domain.repository.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Module Koin untuk dependency data layer.
 * Bertanggung jawab menyediakan Retrofit, ApiService, Repository.
 * Menggunakan base URL VPS (codasuaka.my.id).
 */
val dataModule = module {
    // OkHttp Client dengan interceptor
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (com.example.codasuaka.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = AuthInterceptor(get()) // TokenManager dari Koin

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance — menggunakan BASE_URL dari BuildConfig
    single {
        Retrofit.Builder()
            .baseUrl(com.example.codasuaka.BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService
    single {
        get<Retrofit>().create(ApiService::class.java)
    }

    // ── Repository Bindings ──

    single<AuthRepository> {
        AuthRepositoryImpl(
            apiService = get(),
            tokenManager = get()
        )
    }

    single<ChatRepository> {
        ChatRepositoryImpl(apiService = get())
    }

    single<DashboardRepository> {
        DashboardRepositoryImpl(apiService = get())
    }

    single<OutletRepository> {
        OutletRepositoryImpl(apiService = get())
    }

    single<KaryawanRepository> {
        KaryawanRepositoryImpl(apiService = get())
    }

    single<DivisiRepository> {
        DivisiRepositoryImpl(apiService = get())
    }

    single<PresensiRepository> {
        PresensiRepositoryImpl(apiService = get())
    }

    single<PengajuanRepository> {
        PengajuanRepositoryImpl(apiService = get())
    }

    single<JadwalRepository> {
        JadwalRepositoryImpl(apiService = get())
    }

    single<PenugasanRepository> {
        PenugasanRepositoryImpl(apiService = get())
    }

    // Keuangan
    single<KeuanganRepository> {
        KeuanganRepositoryImpl(apiService = get())
    }
}
