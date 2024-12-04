package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TwistViewModel : ViewModel() {

    // Estado de la lista de Twists
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists

    // Estado de la lista de Questions
    private val _questions = MutableStateFlow<List<QuestionModel>>(emptyList())
    val questions: StateFlow<List<QuestionModel>> = _questions

    // Función para crear un nuevo Twist
    fun createTwist(title: String, description: String) {
        val newTwist = TwistModel(title = title, description = description)
        _twists.value += newTwist
    }

    // Función para editar un Twist existente
    fun editTwist(id: String, newTitle: String, newDescription: String) {
        _twists.value = _twists.value.map { twist ->
            if (twist.id == id) {
                twist.copy(
                    title = newTitle,
                    description = newDescription,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                twist
            }
        }
    }

    // Función para eliminar un Twist
    fun deleteTwist(id: String) {
        _twists.value = _twists.value.filter { it.id != id }
    }

    // Función para crear una nueva Pregunta
    fun createQuestion(questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(question = questionText, answers = answers)
        _questions.value += newQuestion
    }

    // Función para editar una Pregunta existente
    fun editQuestion(id: String, newQuestionText: String, newAnswers: List<AnswerModel>) {
        _questions.value = _questions.value.map { question ->
            if (question.id == id) {
                question.copy(
                    question = newQuestionText,
                    answers = newAnswers
                )
            } else {
                question
            }
        }
    }

    // Función para eliminar una Pregunta
    fun deleteQuestion(id: String) {
        _questions.value = _questions.value.filter { it.id != id }
    }
}
