package com.grupo18.twister.core.viewmodel

import androidx.compose.runtime.*;
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.twists.Question
import kotlinx.coroutines.launch

class RoomViewModel : ViewModel() {

    val api = ApiClient.realTimeApi

    private val _users = mutableStateOf<Map<String, UserModel>>(emptyMap())
    val users: State<Map<String, UserModel>> get() = _users

    private val _question = mutableStateOf<Question?>(null)
    val question: State<Question?> get() = _question

    // Cargar los usuarios de la sala
    fun loadUsersInRoom(roomId: String) {
        viewModelScope.launch {
            val roomUsers = api.getUsersInRoom(roomId)
            _users.value = roomUsers
        }
    }

    // Actualizar el estado de un usuario
    fun updateUserStatus(roomId: String, userId: String, status: String) {
        viewModelScope.launch {
            api.updateUserStatus(roomId, userId, status)
            // Aquí puedes enviar una actualización en tiempo real al servidor a través del WebSocket
        }
    }

    // Actualizar el puntaje de un usuario
    fun updateUserScore(roomId: String, userId: String, score: Int) {
        viewModelScope.launch {
            api.updateUserScore(roomId, userId, score)
            // Notificar al servidor que se ha actualizado el puntaje
        }
    }

    // Enviar nueva pregunta a la sala
    fun sendNewQuestion(roomId: String, question: Question) {
        viewModelScope.launch {
            api.sendNewQuestion(roomId, question)
            // Notificar al servidor para que se envíe la nueva pregunta a todos los clientes
        }
    }

    fun listenForNewQuestion(roomId: String) {
        viewModelScope.launch {
            try {
                val newQuestion = api.listenForNewQuestion(roomId)
                _question.value = newQuestion
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendEvent(event: Event) {
        viewModelScope.launch{
            try {
                api.sendEvent(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

