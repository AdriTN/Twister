package com.grupo18.twister.models.network.responses

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val game: GameResponse,
    val pin: Int
)
