package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.models.QuestionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import java.util.UUID

class QuestionViewModel : ViewModel() {

    private val _questionsByTwist = MutableStateFlow<Map<String, List<QuestionModel>>>(emptyMap())

    fun getQuestionsForTwist(twistId: String): StateFlow<List<QuestionModel>> {
        return _questionsByTwist
            .map { it[twistId] ?: emptyList() }
            .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    }

    fun createQuestion(twistId: String, questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(
            id = UUID.randomUUID().toString(),
            question = questionText,
            answers = answers
        )
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions + newQuestion
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun editQuestion(twistId: String, questionId: String, questionText: String, answers: List<AnswerModel>) {
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.map { question ->
            if (question.id == questionId) {
                question.copy(question = questionText, answers = answers)
            } else {
                question
            }
        }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun deleteQuestion(twistId: String, questionId: String) {
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.filter { it.id != questionId }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun hasAtLeastOneCorrectAnswer(twistId: String): Boolean {
        val questions = _questionsByTwist.value[twistId] ?: return false
        if (questions.isEmpty()) return false
        return questions.any { question -> question.answers.any { it.isCorrect } }
    }
}
