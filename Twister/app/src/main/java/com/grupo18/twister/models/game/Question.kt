package com.grupo18.twister.models.game

import kotlinx.serialization.Serializable

@Serializable
data class SingleQuestion(
    val description: String,
    val image: String?, // URL de la imagen, si existe
    val options: List<String>,
    val solution: Int // Índice de la opción correcta
)
