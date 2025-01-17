package com.grupo18.twister.models.network.responses

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val message: String,
    val urlId: String
)
