package com.grupo18.twister.models.network.responses

import com.grupo18.twister.models.network.domain.PlayerModel
import kotlinx.serialization.Serializable

@Serializable
data class JoinPinResponse(
    val currentGameId: String,
    val playerId: String,
    val game: GameResponse,
    val playerName: String,
    val imageIndex: String,
    val twistQuestions: String
)
