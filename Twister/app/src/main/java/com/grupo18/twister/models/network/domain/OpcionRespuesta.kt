package com.grupo18.twister.models.network.domain

import kotlinx.serialization.Serializable

@Serializable
data class OpcionRespuesta(
    val isCorrect: Boolean,
    val text: String,
    val id: Int? = null
) : RespuestaRecibida()
