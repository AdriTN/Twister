package com.grupo18.twister.core.models

import kotlinx.serialization.Serializable

@Serializable
data class QuestionModel(
    val id: String = "0000",
    val question: String,
    val answers: List<AnswerModel>
)