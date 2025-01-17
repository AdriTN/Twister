package com.grupo18.twister.models.network.events

import kotlinx.serialization.Serializable

@Serializable
data class AnswerProvidedEvent(
    val playerId: String,
    val answer: String
)
