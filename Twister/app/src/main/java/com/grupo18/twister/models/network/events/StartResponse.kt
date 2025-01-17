package com.grupo18.twister.models.network.events

import kotlinx.serialization.Serializable

@Serializable
data class StartResponse(
    val pinRoom: String
)
