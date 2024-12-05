package com.grupo18.twister.core.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.interfaces.RealTimeApi
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.twists.Question
import com.grupo18.twister.core.screens.twists.RealTimeClient
import io.socket.client.Socket
import kotlinx.coroutines.launch

class RoomViewModel : ViewModel() {

    private val socket: Socket = ApiClient.getSocket() // Método que deberías implementar en ApiClient
    private val realTimeClient = RealTimeClient(socket)
    private val api = RealTimeApi(socket)

    private val _users = mutableStateOf<Map<String, UserModel>>(emptyMap())
    val users: State<Map<String, UserModel>> get() = _users

    private val _question = mutableStateOf<Question?>(null)
    val question: State<Question?> get() = _question

    // Cargar los usuarios de la sala
    fun loadUsersInRoom(roomId: String) {
        viewModelScope.launch {
            try {
                val roomUsers = api.getUsersInRoom(roomId) // Llama a la API
                _users.value = roomUsers // Asigna el resultado a _users
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de errores (puedes mostrar un mensaje o hacer otra cosa)
            }
        }
    }


    // Actualizar el estado de un usuario
    fun updateUserStatus(roomId: String, userId: String, status: String) {
        viewModelScope.launch {
            ApiClient.realTimeApi.updateUserStatus(roomId, userId, status)
            // Aquí puedes enviar una actualización en tiempo real al servidor a través del WebSocket
        }
    }

    // Actualizar el puntaje de un usuario
    fun updateUserScore(roomId: String, userId: String, score: Int) {
        viewModelScope.launch {
            ApiClient.realTimeApi.updateUserScore(roomId, userId, score)
            // Notificar al servidor que se ha actualizado el puntaje
        }
    }

    // Enviar nueva pregunta a la sala
    fun sendNewQuestion(roomId: String, question: Question) {
        viewModelScope.launch {
            ApiClient.realTimeApi.sendNewQuestion(roomId, question)
            // Notificar al servidor para que se envíe la nueva pregunta a todos los clientes
        }
    }

    // Escuchar preguntas nuevas a través del WebSocket
    fun listenForNewQuestions(roomId: String) {
        realTimeClient.listenForEvents(roomId) { event ->
            // Aquí manejas el evento que has recibido
            if (event.message.startsWith("New question:")) {
                val newQuestion = null // Método que deberás implementar
                _question.value = newQuestion
            }
        }
    }

    fun sendEvent(event: Event) {
        realTimeClient.sendEvent(event) // Ahora se envía a través del socket
    }
}
