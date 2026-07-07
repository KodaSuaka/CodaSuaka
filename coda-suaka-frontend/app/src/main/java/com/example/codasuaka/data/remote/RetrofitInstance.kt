package com.example.codasuaka.data.remote

import com.example.codasuaka.core.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @deprecated Gunakan Retrofit dari Koin DI module (`dataModule`)
 * yang sudah include AuthInterceptor dan logging yang proper.
 * Instance ini TIDAK memiliki interceptor auth sehingga tidak bisa
 * mengirim request terautentikasi.
 */
@Deprecated("Gunakan ApiService dari Koin DI module yang sudah include AuthInterceptor")
object RetrofitInstance {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
