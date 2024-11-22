package com.grupo18.twister.core.screens.authentication

import android.app.Application
import com.grupo18.twister.core.models.UserModel

class MyApp : Application() {
    // Variable global para almacenar el usuario actual
    var currentUser: UserModel? = null
        private set

    fun getUser(): UserModel? {
        return currentUser
    }

    // Función para guardar un usuario completo
    fun saveUser(user: UserModel) {
        currentUser = user
    }

    // Función para actualizar datos del usuario
    fun updateUser(update: (UserModel) -> UserModel) {
        currentUser?.let {
            currentUser = update(it)
        }
    }

    // Función para actualizar solo el token
    fun updateToken(newToken: String) {
        currentUser?.let {
            currentUser = it.copy(token = newToken)
        }
    }

    // Función para borrar el usuario actual (logout)
    fun clearUser() {
        currentUser = null
    }

    fun changeAvatar(newUrl: String) {
        currentUser?.let {
            currentUser = it.copy(avatarUrl = newUrl)
        }
    }
}
