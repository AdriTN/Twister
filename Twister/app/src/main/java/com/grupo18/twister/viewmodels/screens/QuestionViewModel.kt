// Archivo: QuestionViewModel.kt
package com.grupo18.twister.viewmodels.screens

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.grupo18.twister.core.network.RetrofitClient
import com.grupo18.twister.core.network.services.TwistService
import com.grupo18.twister.models.game.AnswerModel
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.models.game.TwistModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionViewModel(
    private var twist: TwistModel
) : ViewModel() {
    private val twistService: TwistService = RetrofitClient.retrofit.create(TwistService::class.java)
    private val _questions = MutableStateFlow(twist.twistQuestions)
    val questions: StateFlow<List<QuestionModel>> = _questions

    // Método para crear una pregunta
    fun createQuestion(scope: CoroutineScope, questionText: String, answers: List<AnswerModel>) {
        scope.launch {
            val newQuestion = QuestionModel(
                id = generateUniqueId(),
                question = questionText,
                answers = answers
            )
            // Actualizar el twist con la nueva pregunta
            twist = twist.copy(twistQuestions = twist.twistQuestions + newQuestion)
            _questions.value = twist.twistQuestions
        }
    }

    // Método para editar una pregunta
    fun editQuestion(scope: CoroutineScope, questionId: String, questionText: String, answers: List<AnswerModel>) {
        scope.launch {
            val updatedQuestions = twist.twistQuestions.map { question ->
                if (question.id == questionId) {
                    question.copy(question = questionText, answers = answers)
                } else {
                    question
                }
            }
            // Actualizar el twist con las preguntas modificadas
            twist = twist.copy(twistQuestions = updatedQuestions)
            _questions.value = twist.twistQuestions
        }
    }

    // Método para eliminar una pregunta
    fun deleteQuestion(scope: CoroutineScope, questionId: String) {
        scope.launch {
            val updatedQuestions = twist.twistQuestions.filter { it.id != questionId }
            // Actualizar el twist sin la pregunta eliminada
            twist = twist.copy(twistQuestions = updatedQuestions)
            _questions.value = twist.twistQuestions
        }
    }

    // Verificar si hay al menos una respuesta correcta
    fun hasAtLeastOneCorrectAnswer(): Boolean {
        return _questions.value.any { question ->
            question.answers.any { answer -> answer.isCorrect }
        }
    }

    // Guardar cambios y enviar el twist actualizado a la API
    fun saveChanges(
        scope: CoroutineScope,
        token: String,
        callback: (Boolean) -> Unit
    ) {
        scope.launch {
            val gson = Gson()
            val twistJson = gson.toJson(twist)
            println("Se va a guardar el nuevo twist: $twistJson")
            val call = twistService.editTwist(token, twistData = twist)
            call.enqueue(object : Callback<TwistModel> {
                override fun onResponse(call: Call<TwistModel>, response: Response<TwistModel>) {
                    if (response.isSuccessful) {
                        println("Twist editado con éxito: ${response.body()}")
                        callback(true)
                    } else {
                        println("Error en la respuesta: ${response.code()} - ${response.message()}")
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<TwistModel>, t: Throwable) {
                    println("Fallo al editar el twist: ${t.message}")
                    callback(false)
                }
            })
        }
    }
    // Generar un ID único para nuevas preguntas
    private fun generateUniqueId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
