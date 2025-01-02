package com.grupo18.twister.core.models


data class TwistModel(
    val id: String = "00000",
    val title: String,
    val description: String,
    var imageUri: String? = null,
    val twistQuestions: List<QuestionModel> = emptyList(),
    val isPublic: Boolean = false
)
