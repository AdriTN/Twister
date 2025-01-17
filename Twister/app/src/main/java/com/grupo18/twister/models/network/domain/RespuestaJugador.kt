package com.grupo18.twister.models.network.domain

import kotlinx.serialization.Serializable

@Serializable
data class RespuestaJugador(
    val playerName: String,
    val answer: String
) : RespuestaRecibida()
