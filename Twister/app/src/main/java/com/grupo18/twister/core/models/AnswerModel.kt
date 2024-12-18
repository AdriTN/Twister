package com.grupo18.twister.core.models

import kotlinx.serialization.Serializable

@Serializable
data class AnswerModel(
    val text: String,
    val isCorrect: Boolean
)