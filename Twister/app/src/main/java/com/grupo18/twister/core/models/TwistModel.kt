package com.grupo18.twister.core.models

data class ImageUri(val uri: String)


data class TwistModel(
    val id: String = "00000",
    val title: String,
    val description: String,
    val imageUri: ImageUri? = null,
    val twistQuestions: List<QuestionModel> = emptyList()
)
