package com.grupo18.twister.models.network.domain

import kotlinx.serialization.Serializable

@Serializable
data class PlayerModel(
    val id: String,
    val imageIndex: String,
    val socketId: String? = ""
)
