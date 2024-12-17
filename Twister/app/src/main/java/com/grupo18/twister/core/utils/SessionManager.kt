package com.grupo18.twister.core.utils

import android.content.Context
import com.grupo18.twister.core.models.UserModel

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    // Guardar sesión del usuario
    fun saveSession(user: UserModel) {
        editor.putString("user_token", user.token)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    // Obtener sesión del usuario
    fun getSession(): UserModel? {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null

        val token = sharedPreferences.getString("user_token", null)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)

        return if (token != null && username != null && email != null) {
            UserModel(token = token, username = username, email = email, password = "")
        } else null
    }

    // Borrar sesión del usuario (logout)
    fun logout() {
        editor.clear()
        editor.apply()
    }
}
