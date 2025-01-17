package com.grupo18.twister.models.network.events

import kotlinx.serialization.Serializable

@Serializable
data class GameOverEvent(
    val winnerId: String,
    val finalScores: Map<String, Int>? = null,
    val roomId: String
)
