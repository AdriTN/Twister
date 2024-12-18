package com.grupo18.twister.core.screens.twists

import kotlinx.serialization.Serializable

@Serializable
data class SingleQuestion(
    val description: String,
    val image: String?, // URL de la imagen, si existe
    val options: List<String>,
    val solution: Int // Índice de la opción correcta
)

@Serializable
data class Question(
    val id: Int,
    val description: String,
    val image: String?, // URL de la imagen, si existe
    val questions: List<SingleQuestion>
)
