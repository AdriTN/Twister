package com.grupo18.twister.core.interfaces

import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.twists.Question
import io.socket.client.Socket
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RealTimeApi(private val socket: Socket) {

    init {
        // Conectar el socket al inicio
        socket.connect()
    }

    // Funciones relacionadas con usuarios
    suspend fun getUsersInRoom(roomId: String): Map<String, UserModel> {
        return suspendCoroutine { continuation ->
            socket.emit("getUsersInRoom", roomId)

            socket.on("usersInRoomResponse") { args ->
                val users = args[0] as Map<String, UserModel>
                continuation.resume(users) // Continuar con el resultado
            }
        }
    }


    fun updateUserStatus(roomId: String, userId: String, status: String) {
        socket.emit("updateUserStatus", roomId, userId, status)
    }

    fun updateUserScore(roomId: String, userId: String, score: Int) {
        socket.emit("updateUserScore", roomId, userId, score)
    }

    // Funciones relacionadas con preguntas
    fun sendNewQuestion(roomId: String, question: Question) {
        socket.emit("sendNewQuestion", roomId, question)
    }

    fun listenForNewQuestion(roomId: String, callback: (Question) -> Unit) {
        socket.emit("listenForNewQuestion", roomId)

        socket.on("newQuestion") { args ->
            val question = args[0] as Question
            callback(question)
        }
    }

    // Funciones relacionadas con eventos
    fun getEventsLongPolling(roomId: String, callback: (List<Event>) -> Unit) {
        socket.emit("getEventsLongPolling", roomId)

        socket.on("eventsResponse") { args ->
            val events = args[0] as List<Event>
            callback(events)
        }
    }

    fun sendEvent(event: Event) {
        socket.emit("sendEvent", event)
    }
}
