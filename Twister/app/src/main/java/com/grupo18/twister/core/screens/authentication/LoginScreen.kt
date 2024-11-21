package com.grupo18.twister.core.screens.authentication


import android.widget.Toast
import android.util.Patterns
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo18.twister.core.models.UserModel


@Composable
fun LoginScreen(
    onLoginSuccess: (UserModel) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel()
    val scope = rememberCoroutineScope()

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
        Text(text = "Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

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
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Validar campos vacíos
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Validar formato del email
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        val authManager = AuthManager { result ->
                            result.onSuccess { user ->
                                if (user != null) {
                                    println("Login successful! Entering to mainviewmodel")
                                    println("Token: ${user.token}")
                                    onLoginSuccess(
                                        UserModel(
                                            token = user.token.toString(),
                                            username = user.username.toString(),
                                            email = email,
                                            password = password,
                                            age = 0,
                                        )
                                    )
                                    showToast("Login successful!")
                                } else {
                                    showToast("Login failed!")
                                }
                            }.onFailure { exception ->
                                errorMessage = exception.message ?: "An error occurred"
                            }
                        }
                        authManager.signIn(email, password)
                    } else {
                        errorMessage = "Invalid email format"
                    }
                } else {
                    errorMessage = "Please fill all fields"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Don't have an account? Sign Up",
            color = Color.Blue,
            modifier = Modifier.clickable { onSwitchToRegister() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val authManager = AuthManager { result ->
                    result.onSuccess { user ->
                        if (user != null) {
                            println("Login successful! Entering to mainviewmodel")
                            println("Token: ${user.token}")
                            onLoginSuccess(
                                UserModel(
                                    token = user.token.toString(),
                                    username = user.username.toString(),
                                    email = email,
                                    password = password,
                                )
                            )
                            showToast("Login successful!")
                        } else {
                            showToast("Login failed!")
                        }
                    }.onFailure { exception ->
                        errorMessage = exception.message ?: "An error occurred"
                    }
                }
                authManager.signInAnonymously()
            },
            modifier = Modifier
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF7E7E7E))
        ) {
            Text(
                text = "Ingresar como Anónimo",
                color = Color.White,
            )
        }
    }
}
