package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.UserModel
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // Crear un nuevo usuario
    @POST("/users")
      fun createUser(@Body user: UserModel): Call<UserModel>

    // Obtener un usuario por ID
    @GET("/users/{id}")
    fun getUser(@Path("id") id: String): Call<UserModel>
}