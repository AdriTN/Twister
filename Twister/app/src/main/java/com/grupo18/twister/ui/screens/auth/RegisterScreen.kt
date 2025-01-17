package com.grupo18.twister.ui.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.features.auth.AuthManager
import com.grupo18.twister.models.common.UserModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (UserModel) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }  // Variable para el nombre de usuario
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val app = context.applicationContext as MyApp


    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Sign Up", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo para el nombre de usuario
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text("Repeat Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && password == repeatPassword && username.isNotEmpty()) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        val authManager = AuthManager { result ->
                            result.onSuccess { user ->
                                if (user != null) {
                                    val newUser = UserModel(
                                        token = user.token.toString(),
                                        username = username,
                                        email = email,
                                        password = password,
                                        age = 0,
                                    )
                                    app.saveUser(newUser)
                                    onRegisterSuccess(newUser)
                                    showToast("Registration successful!")
                                } else {
                                    showToast("Failed to register user!")
                                }
                            }.onFailure { exception ->
                                errorMessage = exception.message
                                println("Error: $errorMessage")
                            }
                        }
                        authManager.createUser(email, password, username)
                    } else {
                        errorMessage = "Invalid email format"
                    }
                } else {
                    errorMessage = "Please fill all fields correctly"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Already have an account? Login",
            color = Color.Blue,
            modifier = Modifier.clickable { onSwitchToLogin() }
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }
    }
}
