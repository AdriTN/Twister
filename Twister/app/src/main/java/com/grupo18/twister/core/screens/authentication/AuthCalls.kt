package com.grupo18.twister.core.screens.authentication

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.TokenVerificationResponse
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AuthManager(
    private val onResult: (Result<UserResponse?>) -> Unit
) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)
    private var token: String? = null

    /**
     * Crea un nuevo usuario con el email, contraseña y nombre de usuario proporcionados.
     * Requiere un token válido.
     */
    fun createUser(email: String, password: String, username: String) {
//        if (token.isNullOrEmpty()) {
//            handler.post { onResult(Result.failure(Exception("Token no válido o ausente"))) }
//            return
//        }

        executor.execute {
            val result = try {
                val user = UserModel("", username, email, password)
                val response = apiService.createUser(user).execute()
                println("Respuesta del servidor: ${response.body()}")
                if (response.isSuccessful) {
                    user.token = response.body()?.token ?: ""
                    response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
                } else {
                    Result.failure(Exception("Error del servidor: ${response.code()} ${response.errorBody()?.string()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Error de red: ${e.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }

            handler.post {
                onResult(result)
            }
        }
    }

    /**
     * Inicia sesión con las credenciales proporcionadas.
     * Actualiza el token si la autenticación es exitosa.
     */
    fun signIn(email: String, password: String) {
        executor.execute {
            val result = try {
                println("Intentando iniciar sesión con email: $email, password: $password")
                val response = apiService.getUser(LoginRequest(email, password)).execute()
                println("Respuesta del servidor: ${response.body()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    token = user?.token
                    user?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
                } else {
                    Result.failure(Exception("Error del servidor: ${response.code()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Error de red: ${e.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }

            handler.post {
                onResult(result)
            }
        }
    }

    /**
     * Inicia sesión como usuario anónimo.
     * Actualiza el token tras autenticación exitosa.
     */
    fun signInAnonymously() {
        executor.execute {
            val result = try {
                val response = apiService.getAnonymousUser().execute()
                if (response.isSuccessful) {
                    val token = response.body()?.token ?: throw Exception("Token no encontrado")
                    this.token = token
                    val anonymousUser = UserResponse(token = token, message = null)
                    Result.success(anonymousUser)
                } else {
                    Result.failure(Exception("Error del servidor: ${response.code()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Error de red: ${e.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }

            handler.post {
                onResult(result)
            }
        }
    }

    /**
     * Obtiene el token actual.
     */
    fun verifyToken(token: String) {
        apiService.verifyToken("Bearer $token").enqueue(object : Callback<TokenVerificationResponse> {
            override fun onResponse(call: Call<TokenVerificationResponse>, response: Response<TokenVerificationResponse>) {
                if (response.isSuccessful) {
                    val verificationResponse = response.body()
                    Log.d("TOKEN_VERIFY", "Respuesta: ${verificationResponse?.message}")
                    verificationResponse?.decoded?.let {
                        Log.d("TOKEN_VERIFY", "Token decodificado: $it")
                    }
                } else {
                    Log.e("TOKEN_VERIFY", "Token inválido: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TokenVerificationResponse>, t: Throwable) {
                Log.e("TOKEN_VERIFY", "Error de red: ${t.message}")
            }
        })
    }

}
