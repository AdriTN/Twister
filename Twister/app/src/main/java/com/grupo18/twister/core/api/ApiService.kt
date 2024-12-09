package com.grupo18.twister.core.api

import com.grupo18.twister.core.models.GameResponse
import com.grupo18.twister.core.models.TokenVerificationResponse
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.models.UserResponse
import com.grupo18.twister.core.models.LoginRequest
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.TwistRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

    @GET("/games/create")
    fun createGame(@Header("Authorization") token: String): Call<List<TwistModel>>

    @GET("/games/join")
    fun joinGame(@Header("Authorization") token: String): Call<GameResponse>

    // Endpoint para obtener todas las preguntas
    @GET("/games/get/{id}")
    fun getAllQuestions(@Header("Authorization") token: String): Call<List<QuestionModel>>

    // Endpoint para obtener todas las preguntas de un usuario
    @GET("/twists/get")
    fun getUserTwists(@Header("Authorization") token: String): Call<TwistRequest>

    // Endpoint para crear una nueva pregunta
    @POST("/twists/create")
    fun createQuestion(
        @Header("Authorization") token: String,
        @Body question: QuestionModel
    ): Call<QuestionModel>

    // Endpoint para editar un quizz existente
    @PUT("/twists/edit/{id}")
    fun editTwist(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body twistData: TwistModel // Cambia aquí si es necesario
    ): Call<TwistModel> // Asegúrate de que esto coincida con el modelo que esperas

    @DELETE("/twists/delete/{id}")
    fun deleteTwist(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    @Multipart
    @POST("/images/upload")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>

    @GET("/images/download/{fileName}")
    fun downloadImage(@Path("fileName") fileName: String): Call<ResponseBody>
}