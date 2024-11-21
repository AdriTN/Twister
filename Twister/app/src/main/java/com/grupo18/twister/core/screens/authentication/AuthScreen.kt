package com.grupo18.twister.core.screens.authentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import com.grupo18.twister.core.models.UserModel


@Composable
fun AuthScreen(onAuthSuccess: (UserModel) -> Unit) {
    var isSignUpMode by remember { mutableStateOf(false) }

    if (isSignUpMode) {
        RegisterScreen(
            onRegisterSuccess = { user ->
                onAuthSuccess(user)
            },
            onSwitchToLogin = { isSignUpMode = false }
        )
    } else {
        LoginScreen(
            onLoginSuccess = { user ->
                onAuthSuccess(user)
            },
            onSwitchToRegister = { isSignUpMode = true }
        )
    }
}
