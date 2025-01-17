package com.grupo18.twister.core.network.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

object SocketManager {
    private var socket: Socket? = null
    private const val SOCKET_BASE_URL = "http://192.168.1.16:3001/"

    fun connect() {
        if (socket == null) {
            try {
                socket = IO.socket(SOCKET_BASE_URL)
            } catch (e: URISyntaxException) {
                Log.e("SocketManager", "URISyntaxException: ${e.message}")
                return
            }
        }

        socket?.apply {
            // Listener para conexión exitosa
            on(Socket.EVENT_CONNECT, onConnect)
            // Listener para errores de conexión
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            // Listener para desconexiones
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            connect()
        }
    }

    fun getSocket(): Socket {
        return socket ?: throw IllegalStateException("Socket not initialized")
    }

    // Listeners
    private val onConnect = Emitter.Listener {
        Log.i("SocketManager", "Socket connected successfully")
    }

    private val onConnectError = Emitter.Listener { args ->
        Log.e("SocketManager", "Socket connection error: ${args.joinToString()}")
    }

    private val onDisconnect = Emitter.Listener {
        Log.i("SocketManager", "Socket disconnected")
    }

    // Opcional: métodos para desconectar
    fun disconnect() {
        socket?.disconnect()
        socket?.off(Socket.EVENT_CONNECT, onConnect)
        socket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
    }
}
