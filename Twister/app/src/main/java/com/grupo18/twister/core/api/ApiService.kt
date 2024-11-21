package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.TokenVerificationResponse
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.models.UserResponse
import com.grupo18.twister.core.screens.authentication.LoginRequest
import retrofit2.Call
import retrofit2.http.*


interface ApiService {
    @POST("/users/register")
    fun createUser(@Body user: UserModel): Call<UserResponse>

    @POST("/users/login")
    fun getUser(@Body request: LoginRequest): Call<UserResponse>

    @POST("/users/login/anonymous")
    fun getAnonymousUser(): Call<UserResponse>

    @GET("/users/verify")
    fun verifyToken(@Header("Authorization") token: String): Call<TokenVerificationResponse>

    @GET("/users/{id}")
    fun getUser(@Path("id") id: String): Call<UserModel>
}