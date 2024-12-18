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
    val imageIndex: String,
    val socketId: String? = ""
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
data class JoinPinResponse(
    val currentGameId: String,
    val playerId: String,
    val game: GameResponse,
    val playerName: String,
    val imageIndex: String
)

@Serializable
data class JoinResponse(
    val currentGameId: String,
    val playerId: String,
    val playerList: List<PlayerModel>,
    val game: GameResponse
)

data class UploadResponse(
    val message: String,
    val urlId: String
)

data class TwistRequest(
    val message: String,
    val twists: List<TwistModel>
)

data class PlayersLeftList(
    val roomId: String,
    val players: List<PlayerModel>
)
