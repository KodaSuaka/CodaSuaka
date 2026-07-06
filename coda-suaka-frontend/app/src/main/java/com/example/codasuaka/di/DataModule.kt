package com.example.codasuaka.di

import com.example.codasuaka.core.utils.Constants
import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.interceptor.AuthInterceptor
import com.example.codasuaka.data.repository.AuthRepositoryImpl
import com.example.codasuaka.data.repository.ChatRepositoryImpl
import com.example.codasuaka.domain.repository.AuthRepository
import com.example.codasuaka.domain.repository.ChatRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Module Koin untuk dependency data layer.
 * Bertanggung jawab menyediakan Retrofit, ApiService, Repository.
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

    // Retrofit instance
    single {
        Retrofit.Builder()
            .baseUrl("${Constants.BASE_URL}/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService
    single {
        get<Retrofit>().create(ApiService::class.java)
    }

    // Repository Implementation (bind ke interface)
    single<AuthRepository> {
        AuthRepositoryImpl(
            apiService = get(),
            tokenManager = get()
        )
    }

    // Chat Repository
    single<ChatRepository> {
        ChatRepositoryImpl(apiService = get())
    }
}
