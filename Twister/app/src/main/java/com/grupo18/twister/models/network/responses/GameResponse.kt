package com.grupo18.twister.models.network.responses

import com.grupo18.twister.models.network.domain.PlayerModel
import kotlinx.serialization.Serializable

@Serializable
data class GameResponse(
    val id: String,
    val adminId: Int? = null,
    val createdAt: Long,
    val socket: String,
    val players: List<PlayerModel>,
    val twistId: String
)
