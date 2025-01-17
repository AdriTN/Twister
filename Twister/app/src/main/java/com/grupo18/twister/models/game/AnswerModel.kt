package com.grupo18.twister.models.game

import kotlinx.serialization.Serializable

@Serializable
data class AnswerModel(
    val text: String,
    val isCorrect: Boolean
)