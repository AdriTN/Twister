package com.grupo18.twister.core.screens.authentication


import CreateUserTask
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.navigation.LoginData
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: (LoginData) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var password2Visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val loginData = remember { mutableStateOf<LoginData?>(null) }

    CreateUserTask(
        email = "email@prueba.com",
        password = "password123",
        username = "username1"
    ) { result ->
        println("Felicidades nena, tienes este result $result")
        if (result.isSuccess) {
            val user = result.getOrNull()
            onAuthSuccess(LoginData(email, password)) // Lógica de éxito
        } else {
            val error = result.exceptionOrNull()?.message
            errorMessage = error // Lógica de manejo de error
        }
    }.execute()


    fun resetFields() {
        email = ""
        password = ""
        repeatPassword = ""
        acceptTerms = false
        emailError = false
        passwordError = false
        repeatPasswordError = false
        errorMessage = null
        passwordVisible = false
        password2Visible = false
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Manejo del éxito en el inicio de sesión
    LaunchedEffect(loginData.value) {
        loginData.value?.let {
            onAuthSuccess(it)
            resetFields()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUpMode) "Sign Up" else "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Email
        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Contraseña
        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        if (isSignUpMode) {
            Spacer(modifier = Modifier.height(8.dp))

            // Campo de Repetir Contraseña
            TextField(
                value = repeatPassword,
                onValueChange = {
                    repeatPassword = it
                    repeatPasswordError = false
                },
                label = { Text("Repeat Password") },
                singleLine = true,
                visualTransformation = if (password2Visible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (password2Visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { password2Visible = !password2Visible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Sign Up / Login
        Button(
            onClick = {
                emailError = email.isEmpty() || !isValidEmail(email)
                passwordError = password.isEmpty()
                repeatPasswordError = isSignUpMode && (password != repeatPassword)

                if (emailError || passwordError || repeatPasswordError) {
                    errorMessage = "Please correct the errors"
                    return@Button
                }

                scope.launch {
                    if (isSignUpMode) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    loginData.value = LoginData(email, password)
                                    showToast("Sign up successful!")
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    loginData.value = LoginData(email, password)
                                    showToast("Login successful!")
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isSignUpMode) "Sign Up" else "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSignUpMode) "Already have an account? Login" else "Don't have an account? Sign Up",
            color = Color.Blue,
            modifier = Modifier.clickable {
                resetFields()
                isSignUpMode = !isSignUpMode
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signInAnonymously().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loginData.value = LoginData("Anonymous", "")
                        showToast("Anonymous login successful!")
                    } else {
                        showToast("Anonymous login failed")
                        Log.w("FirebaseAuth", "signInAnonymously:failure", task.exception)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(Color.Gray)
        ) {
            Text(text = "Login Anonymously", color = Color.White)
        }
    }
}
