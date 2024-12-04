package com.example.kaizenspeaking.ui.analyze.data.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL = "https://kaizen-server-hc5od2n5nq-et.a.run.app/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.MINUTES)  // Timeout untuk koneksi
        .readTimeout(15, TimeUnit.MINUTES)     // Timeout untuk membaca data
        .writeTimeout(10, TimeUnit.MINUTES)    // Timeout untuk menulis data
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
