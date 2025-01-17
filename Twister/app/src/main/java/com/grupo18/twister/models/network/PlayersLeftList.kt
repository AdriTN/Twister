package com.grupo18.twister.models.network

import com.grupo18.twister.models.network.domain.PlayerModel
import kotlinx.serialization.Serializable

@Serializable
data class PlayersLeftList(
    val roomId: String,
    val players: List<PlayerModel>
)
