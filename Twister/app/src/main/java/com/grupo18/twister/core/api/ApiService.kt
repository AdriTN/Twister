package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.GameResponse
import com.grupo18.twister.core.models.TokenVerificationResponse
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.models.UserResponse
import com.grupo18.twister.core.models.LoginRequest
import com.grupo18.twister.core.models.TwistModel
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

    @GET("/users/twists")
    fun getUserTwists(@Header("Authorization") token: String): Call<TokenVerificationResponse>

    @GET("/games/create")
    fun createGame(@Header("Authorization") token: String): Call<List<TwistModel>>

    @GET("/games/join")
    fun joinGame(@Header("Authorization") token: String): Call<GameResponse>
}