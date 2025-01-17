package com.grupo18.twister.main

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.grupo18.twister.core.network.socket.SocketManager
import com.grupo18.twister.models.common.UserModel
import com.grupo18.twister.features.auth.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyApp : Application() {

    // StateFlow para mantener el usuario actual
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)

        val savedUser = sessionManager.getSession()
        if (savedUser != null) {
            _currentUser.value = savedUser
        }
        createNotificationChannel()

        // **Mover la conexión del socket aquí**
        SocketManager.connect()
        Log.i("MyApp", "Socket connected? ${SocketManager.getSocket().connected()}")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "twister_channel"
            val channelName = "Twister Notifications"
            val channelDescription = "Notifications for Twister app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Función para obtener el usuario actual como StateFlow
    fun getUser(): StateFlow<UserModel?> = currentUser

    // Función para guardar un usuario completo
    fun saveUser(user: UserModel) {
        _currentUser.value = user
        sessionManager.saveSession(user)
    }

    // Función para actualizar datos del usuario
    fun updateUser(update: (UserModel) -> UserModel) {
        _currentUser.value?.let {
            _currentUser.value = update(it)
        }
    }

    // Función para actualizar solo el token
    fun updateToken(newToken: String) {
        _currentUser.value?.let {
            _currentUser.value = it.copy(token = newToken)
        }
    }

    fun clearUser() {
        _currentUser.value = null
        sessionManager.logout()
    }

    // Función para cambiar el avatar
    fun changeAvatar(newUrl: String) {
        _currentUser.value?.let {
            _currentUser.value = it.copy(avatarUrl = newUrl)
        }
    }
}
