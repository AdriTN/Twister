package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class QuestionViewModel : ViewModel() {

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    private val _questions = MutableStateFlow<List<QuestionModel>>(emptyList())
    val questions: StateFlow<List<QuestionModel>> = _questions

    // Variable para almacenar el token de autenticación
    private var authToken: String? = null

    // Función para establecer el token de autenticación
    fun setAuthToken(token: String) {
        authToken = "Bearer $token"
        fetchAllQuestions()
    }

    // Función para obtener todas las preguntas desde la API
    fun fetchAllQuestions() {
        authToken?.let { token ->
            viewModelScope.launch {
                try {
                    val response = apiService.getAllQuestions(token).awaitResponse()
                    if (response.isSuccessful) {
                        _questions.value = response.body() ?: emptyList()
                    } else {
                        // Manejar error
                    }
                } catch (e: Exception) {
                    // Manejar excepción
                }
            }
        }
    }

    // Función para crear una nueva pregunta
    fun createQuestion(questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(question = questionText, answers = answers)
        authToken?.let { token ->
            viewModelScope.launch {
                try {
                    val response = apiService.createQuestion(token, newQuestion).awaitResponse()
                    if (response.isSuccessful) {
                        // Actualizar la lista de preguntas
                        fetchAllQuestions()
                    } else {
                        // Manejar error
                    }
                } catch (e: Exception) {
                    // Manejar excepción
                }
            }
        }
    }

    // Función para editar una pregunta existente
    fun editQuestion(id: String, questionText: String, answers: List<AnswerModel>) {
        val updatedQuestion = QuestionModel(id = id, question = questionText, answers = answers)
        authToken?.let { token ->
            viewModelScope.launch {
                try {
                    val response = apiService.editQuestion(token, id, updatedQuestion).awaitResponse()
                    if (response.isSuccessful) {
                        // Actualizar la lista de preguntas
                        fetchAllQuestions()
                    } else {
                        // Manejar error
                    }
                } catch (e: Exception) {
                    // Manejar excepción
                }
            }
        }
    }

    // Función para eliminar una pregunta
    fun deleteQuestion(id: String) {
        authToken?.let { token ->
            viewModelScope.launch {
                try {
                    val response = apiService.deleteQuestion(token, id).awaitResponse()
                    if (response.isSuccessful) {
                        // Actualizar la lista de preguntas
                        fetchAllQuestions()
                    } else {
                        // Manejar error
                    }
                } catch (e: Exception) {
                    // Manejar excepción
                }
            }
        }
    }
}
