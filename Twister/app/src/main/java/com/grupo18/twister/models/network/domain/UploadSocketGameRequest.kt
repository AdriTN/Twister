package com.grupo18.twister.models.network.domain

import kotlinx.serialization.Serializable

@Serializable
data class UploadSocketGameRequest(
    val twistId: String,
    val roomId: String
)
