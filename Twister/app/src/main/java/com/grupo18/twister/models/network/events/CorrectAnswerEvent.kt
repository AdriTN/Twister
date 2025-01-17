package com.grupo18.twister.models.network.events

import com.grupo18.twister.models.game.AnswerModel
import kotlinx.serialization.Serializable

@Serializable
data class CorrectAnswerEvent(
    val correctAnswer: AnswerModel,
    val score: Int
)
