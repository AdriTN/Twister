package com.grupo18.twister.core.interfaces

import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.twists.Question
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RealTimeApi {

    // Funciones relacionadas con usuarios
    @GET("rooms/{roomId}/users")
    suspend fun getUsersInRoom(@Path("roomId") roomId: String): Map<String, UserModel>

    @POST("rooms/{roomId}/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("roomId") roomId: String,
        @Path("userId") userId: String,
        @Query("status") status: String
    ): Response<Unit>

    @POST("rooms/{roomId}/users/{userId}/score")
    suspend fun updateUserScore(
        @Path("roomId") roomId: String,
        @Path("userId") userId: String,
        @Query("score") score: Int
    ): Response<Unit>

    // Funciones relacionadas con preguntas
    @POST("rooms/{roomId}/questions")
    suspend fun sendNewQuestion(
        @Path("roomId") roomId: String,
        @Body question: Question
    ): Response<Unit>

    @GET("rooms/{roomId}/questions")
    suspend fun listenForNewQuestion(@Path("roomId") roomId: String): Question

    // Funciones relacionadas con eventos
    @GET("rooms/{roomId}/events")
    suspend fun getEventsLongPolling(@Path("roomId") roomId: String): List<Event>

    @POST("events")
    suspend fun sendEvent(@Body event: Event): Response<Unit>
}
