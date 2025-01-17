package com.grupo18.twister.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Idealmente usar BuildConfig o un archivo de config en vez de hardcodear la URL
    private const val BASE_URL = "http://192.168.1.16:3000/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
