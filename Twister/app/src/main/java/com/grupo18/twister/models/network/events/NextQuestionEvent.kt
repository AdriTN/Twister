package com.grupo18.twister.models.network.events

import kotlinx.serialization.Serializable

@Serializable
data class NextQuestionEvent(
    val questionId: String,
    val questionText: String
)
