package com.grupo18.twister.models.network.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val token: String? = null,
    val message: String? = null,
    val username: String? = null
)
