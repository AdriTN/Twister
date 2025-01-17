package com.grupo18.twister.features.auth

import android.os.Handler
import android.os.Looper
import com.grupo18.twister.core.network.RetrofitClient
import com.grupo18.twister.core.network.services.UserService
import com.grupo18.twister.models.common.UserModel
import com.grupo18.twister.models.network.responses.UserResponse
import retrofit2.HttpException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AuthManager(
    private val onResult: (Result<UserResponse?>) -> Unit
) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val userService: UserService = RetrofitClient.retrofit.create(UserService::class.java)
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
                val response = userService.createUser(user).execute()
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
                val response = userService.getUser(LoginRequest(email, password)).execute()
                println("Respuesta del servidor: ${response.body()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    token = user?.token
                    user?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
                } else {
                    var errorBody = response.errorBody()?.string()
                    if (response.code() == 401 && errorBody != null) {
                        errorBody = errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                        println("Error del servidor: $errorBody")
                    } else {
                        println("Error del servidor: ${response.code()}")
                    }
                    Result.failure(Exception(errorBody))
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
                println("Intentando iniciar sesión anónima")
                val response = userService.getAnonymousUser().execute()
                println("Respuesta del servidor: ${response.body()}")
                if (response.isSuccessful) {
                    val user = response.body()
                    token = user?.token
                    user?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
                } else {
                    var errorBody = response.errorBody()?.string()
                    if (response.code() == 401 && errorBody != null) {
                        errorBody = errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                        println("Error del servidor: $errorBody")
                    } else {
                        println("Error del servidor: ${response.code()}")
                    }
                    Result.failure(Exception(errorBody))
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

}
