package com.example.codasuaka.di

import com.example.codasuaka.data.local.TokenManager
import com.example.codasuaka.data.remote.ApiService
import com.example.codasuaka.data.remote.interceptor.AuthInterceptor
import com.example.codasuaka.data.repository.*
import com.example.codasuaka.domain.repository.*
import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Gson adapter untuk Int? yang toleran terhadap object JSON.
 * Jika value JSON bukan number (misal object User dari relasi Laravel),
 * return null alih-alih crash dengan "Expected an int but got BEGIN_OBJECT".
 */
private class NullableIntAdapter : JsonDeserializer<Int?>, JsonSerializer<Int?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Int? {
        return try {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) json.asInt else null
        } catch (e: Exception) { null }
    }

    override fun serialize(src: Int?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src)
    }
}

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

    // Gson instance dengan adapter toleran untuk Int?
    // NullableIntAdapter mencegah crash "Expected an int but got BEGIN_OBJECT"
    // saat field `created_by` dari Laravel masih terlanjur berupa object User.
    single {
        GsonBuilder()
            .registerTypeAdapter(Int::class.java, NullableIntAdapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, NullableIntAdapter())
            .registerTypeAdapter(Integer::class.java, NullableIntAdapter())
            .create()
    }

    // Retrofit instance — menggunakan BASE_URL dari BuildConfig
    single {
        Retrofit.Builder()
            .baseUrl(com.example.codasuaka.BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
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
