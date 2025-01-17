package com.grupo18.twister.core.network.services

import com.grupo18.twister.features.auth.LoginRequest
import com.grupo18.twister.models.common.UserModel
import com.grupo18.twister.models.network.responses.UserResponse
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @POST("/users/register")
    fun createUser(
        @Body user: UserModel
    ): Call<UserResponse>

    @POST("/users/login")
    fun getUser(
        @Body request: LoginRequest
    ): Call<UserResponse>

    @POST("/users/login/anonymous")
    fun getAnonymousUser(): Call<UserResponse>
}
