package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

class QuestionViewModel : ViewModel() {

    // Mapa de preguntas por twistId
    private val _questionsByTwist = MutableStateFlow<Map<String, List<QuestionModel>>>(emptyMap())

    // Función para obtener las preguntas de un Twist específico
    fun getQuestionsForTwist(twistId: String): StateFlow<List<QuestionModel>> {
        return _questionsByTwist
            .map { it[twistId] ?: emptyList() }
            .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    }

    // Función para crear una nueva pregunta asociada a un Twist
    fun createQuestion(twistId: String, questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(question = questionText, answers = answers)
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions + newQuestion
        _questionsByTwist.value += (twistId to updatedQuestions)
    }

    // Función para editar una pregunta existente
    fun editQuestion(twistId: String, questionId: String, questionText: String, answers: List<AnswerModel>) {
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.map { question ->
            if (question.id == questionId) {
                question.copy(question = questionText, answers = answers)
            } else {
                question
            }
        }
        _questionsByTwist.value += (twistId to updatedQuestions)
    }

    // Función para eliminar una pregunta
    fun deleteQuestion(twistId: String, questionId: String) {
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.filter { it.id != questionId }
        _questionsByTwist.value += (twistId to updatedQuestions)
    }
}
