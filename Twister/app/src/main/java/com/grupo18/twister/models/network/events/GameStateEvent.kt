package com.grupo18.twister.models.network.events

import kotlinx.serialization.Serializable

// Ejemplo simple; si quieres evitar Any?, puedes usar sealed class en su lugar
@Serializable
data class GameStateEvent(
    val state: String,
    val data: String? = null
)
