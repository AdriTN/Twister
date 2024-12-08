package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.TwistModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class QuestionViewModel(private val apiService: ApiService) : ViewModel() {

    private val _questionsByTwist = MutableStateFlow<Map<String, List<QuestionModel>>>(emptyMap())

    fun getQuestionsForTwist(twistId: String): StateFlow<List<QuestionModel>> {
        return _questionsByTwist
            .map { it[twistId] ?: emptyList() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    fun createQuestion(twistId: String, questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(
            id = UUID.randomUUID().toString(),
            question = questionText,
            answers = answers
        )

        // Añadir la nueva pregunta al estado local
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions + newQuestion
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun editQuestion(twistId: String, questionId: String, questionText: String, answers: List<AnswerModel>) {
        val updatedQuestion = QuestionModel(
            id = questionId,
            question = questionText,
            answers = answers
        )

        // Actualizar la pregunta en el estado local
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.map { question ->
            if (question.id == questionId) updatedQuestion else question
        }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun deleteQuestion(twistId: String, questionId: String) {
        // Eliminar la pregunta del estado local
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.filter { it.id != questionId }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    // Método para guardar cambios en la API
    fun saveChanges(twistId: String, token: String) {
        viewModelScope.launch {
            try {
                // Lógica para enviar las preguntas actuales al servidor
                val questionsToSave = _questionsByTwist.value[twistId] ?: emptyList()
                println("Preguntas a guardar: $questionsToSave")

                // Crear un nuevo objeto TwistModel con los datos necesarios
                val newQuizz = TwistModel(id = twistId, title = "", description = "", twistQuestions = questionsToSave)

                // Realizar la llamada a la API
                val response = apiService.editTwist(token = token, id = twistId, twistData = newQuizz)

                // Manejar la respuesta
                response.enqueue(object : Callback<TwistModel> {
                    override fun onResponse(call: Call<TwistModel>, response: Response<TwistModel>) {
                        if (response.isSuccessful) {
                            val updatedTwist = response.body()
                            // Maneja el "twist" actualizado, por ejemplo, actualizar el estado o mostrar un mensaje
                            println("Twist actualizado exitosamente: $updatedTwist")
                        } else {
                            // Manejar el error de respuesta (ejemplo: mostrar un mensaje al usuario)
                            println("Error al actualizar el twist: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<TwistModel>, t: Throwable) {
                        // Manejar errores de red
                        println("Error de red: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                // Manejar excepciones
                println("Error al guardar cambios: ${e.message}")
            }
        }
    }


    fun hasAtLeastOneCorrectAnswer(twistId: String): Boolean {
        val questions = _questionsByTwist.value[twistId] ?: return false
        if (questions.isEmpty()) return false
        return questions.any { question -> question.answers.any { it.isCorrect } }
    }
}
