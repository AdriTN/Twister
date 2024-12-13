package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --------------------
    // Usuarios (Users)
    // --------------------

    @POST("/users/register")
    fun createUser(
        @Body user: UserModel
    ): Call<UserResponse>

    @POST("/users/login")
    fun getUser(
        @Body request: LoginRequest
    ): Call<UserResponse>

    @POST("/users/login/anonymous")
    fun getAnonymousUser(
    ): Call<UserResponse>

    @GET("/users/verify")
    fun verifyToken(
        @Header("Authorization") token: String
    ): Call<TokenVerificationResponse>

    // --------------------
    // Juegos (Games)
    // --------------------

    @GET("/games/create")
    fun createGame(
        @Header("Authorization") token: String
    ): Call<List<TwistModel>>

    @GET("/games/join")
    fun joinGame(
        @Header("Authorization") token: String
    ): Call<GameResponse>

    @GET("/games/get/{id}")
    fun getAllQuestions(
        @Header("Authorization") token: String,
        @Path("id") gameId: String
    ): Call<List<QuestionModel>>

    // --------------------
    // Giros (Twists)
    // --------------------

    @GET("/twists/get")
    fun getUserTwists(
        @Header("Authorization") token: String
    ): Call<TwistRequest>

    @POST("/twists/create")
    fun createQuestion(
        @Header("Authorization") token: String,
        @Body question: QuestionModel
    ): Call<QuestionModel>

    @PUT("/twists/edit")
    fun editTwist(
        @Header("Authorization") token: String,
        @Body twistData: TwistModel
    ): Call<TwistModel>

    @DELETE("/twists/delete/{id}")
    fun deleteTwist(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    // --------------------
    // Imágenes (Images)
    // --------------------

    @Multipart
    @POST("/images/upload")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("/images/download/{imageUri}")
    fun downloadImage(
        @Path("imageUri") imageUri: String
    ): Call<ResponseBody>

    @HEAD("/images/check/{imageUri}")
    fun checkImageUpdate(
        @Path("imageUri") imageUri: String,
        @Header("If-Modified-Since") lastModified: Long
    ): Call<Void>

    @DELETE("/images/delete")
    fun deleteImage(
        @Header("Authorization") token: String,
        @Body twistData: TwistModel
    ): Call<ResponseBody>
}
