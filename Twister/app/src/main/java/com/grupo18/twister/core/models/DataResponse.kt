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
    val id: String,
    val imageId: String,
)

@Serializable
data class GameResponse(
    val id: String,
    val adminId: Int?,
    val createdAt: Long,
    val socket: String,
    val players: List<PlayerModel>
)

@Serializable
data class RoomResponse(
    val game: GameResponse,
    val pin: Int
)

@Serializable
data class NewUserResponse(
    val playerId: String,
    val playerName: String
)

@Serializable
data class JoinResponse(
    val currentGameId: String,
    val playerId: String,
    val playerName: String,
    val imageId: Int? = 1
)

data class UploadResponse(
    val message: String,
    val urlId: String
)

data class TwistRequest(
    val message: String,
    val twists: List<TwistModel>
)

data class ResultsResponse(
    val results: Map<String, Int>
)