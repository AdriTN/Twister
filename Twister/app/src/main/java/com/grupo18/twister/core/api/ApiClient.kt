package com.grupo18.twister.core.api

import com.grupo18.twister.core.interfaces.RealTimeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://192.168.1.27:3000/"


    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val realTimeApi = retrofit.create(RealTimeApi::class.java)
}
