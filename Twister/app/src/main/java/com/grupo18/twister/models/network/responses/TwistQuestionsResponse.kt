package com.grupo18.twister.models.network.responses

import com.grupo18.twister.models.game.QuestionModel
import kotlinx.serialization.Serializable

@Serializable
data class TwistQuestionsResponse(
    val twistQuestions: List<QuestionModel>
)
