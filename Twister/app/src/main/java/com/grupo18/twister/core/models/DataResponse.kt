package com.grupo18.twister.core.models

import com.grupo18.twister.core.screens.twists.Question
import com.grupo18.twister.core.screens.twists.SingleQuestion
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
    val players: List<PlayerModel>,
    val twistId: String,
)

@Serializable
data class RoomResponse(
    val game: GameResponse,
    val pin: Int,
)

@Serializable
data class JoinPinResponse(
    val currentGameId: String,
    val playerId: String,
    val game: GameResponse,
    val playerName: String,
    val imageIndex: String,
    val twistQuestions: String
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

data class ResultsResponse(
    val results: Map<String, Int>
)

data class GameStateEvent(val state: String, val data: Any?)

@Serializable
data class NextQuestionEvent(val questionId: String, val questionText: String)

@Serializable
data class AnswerProvidedEvent(val playerId: String, val answer: String)

@Serializable
data class GameOverEvent(val winnerId: String, val finalScores: Map<String, Int>)

@Serializable
data class QuestionTimeoutEvent(val questionId: String)

@Serializable
data class UploadSocketGameRequest(val twistId: String, val roomId: String)

@Serializable
data class TwistQuestionsResponse(
    val twistQuestions: List<QuestionModel>
)

@Serializable
data class StartResponse(
    val pinRoom: String
)


@Serializable
sealed class RespuestaRecibida

@Serializable
data class OpcionRespuesta(
    val isCorrect: Boolean,
    val text: String
) : RespuestaRecibida()

@Serializable
data class RespuestaJugador(
    val playerName: String,
    val answer: String
) : RespuestaRecibida()
