package com.grupo18.twister.core.models

data class QuestionModel(
    val id: String = "0000",
    val question: String,
    val answers: List<AnswerModel>
)