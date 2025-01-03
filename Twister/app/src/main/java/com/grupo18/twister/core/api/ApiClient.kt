package com.grupo18.twister.core.api

import com.grupo18.twister.core.interfaces.RealTimeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object ApiClient {
    //private const val BASE_URL = "http://192.168.56.1:3000/"
    //private const val SOCKET_BASE_URL = "http://192.168.56.1:3001/"
    //IP Javier
    private const val BASE_URL = "http://192.168.1.29:3000/"
    private const val SOCKET_BASE_URL = "http://192.168.1.29:3001/"
    private var socket: Socket? = null

    // Inicialización de Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Inicialización de RealTimeApi
    val realTimeApi: RealTimeApi by lazy {
        retrofit.create(RealTimeApi::class.java)
    }

    init {
        try {
            // Inicializa el cliente de Socket.IO
            socket = IO.socket(SOCKET_BASE_URL)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    // Método para obtener la instancia del socket
    fun getSocket(): Socket {
        return socket ?: throw IllegalStateException("Socket has not been initialized.")
    }
}
