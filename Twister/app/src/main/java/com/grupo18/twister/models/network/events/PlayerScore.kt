package com.grupo18.twister.models.network.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerScore(
    @SerialName("player") val name: String,
    val score: Int
)
