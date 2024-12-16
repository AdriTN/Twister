package com.grupo18.twister.core.models

import kotlinx.serialization.Serializable

data class UserResponse(
    val token: String?,
    var message: String?,
    val username: String? = null,
)

data class TokenVerificationResponse(
    val message: String,
    val decoded: DecodedToken?
)

data class DecodedToken(
    val iat: Long,
    val exp: Long,
    val userId: String
)


@Serializable
data class PlayerModel(
    val id: String? = null
)

@Serializable
data class GameResponse(
    val id: String,
    val adminId: String?,
    val createdAt: Long,
    val socket: String,
    val players: List<PlayerModel>
)

@Serializable
data class RoomResponse(
    val game: GameResponse,
    val pin: String
)



data class UploadResponse(
    val message: String,
    val urlId: String
)

data class TwistRequest(
    val message: String,
    val twists: List<TwistModel>
)
