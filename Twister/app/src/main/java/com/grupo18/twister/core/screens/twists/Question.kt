package com.grupo18.twister.core.screens.twists

// Clase para una única pregunta
data class SingleQuestion(
    val description: String,
    val image: String?, // URL de la imagen, si existe
    val options: List<String>,
    val solution: Int // Índice de la opción correcta
)

// Clase contenedora de todas las preguntas
data class Question(
    val id: Int,
    val description: String,
    val image: String?, // URL de la imagen, si existe
    val questions: List<SingleQuestion>
)
