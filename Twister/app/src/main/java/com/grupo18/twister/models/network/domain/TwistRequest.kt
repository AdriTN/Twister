package com.grupo18.twister.models.network.domain

import com.grupo18.twister.models.game.TwistModel

data class TwistRequest(
    val message: String,
    val twists: List<TwistModel>
)
