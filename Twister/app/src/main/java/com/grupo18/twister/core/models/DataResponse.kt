package com.grupo18.twister.core.models

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


data class GameResponse(
    val idGame: String,
)

data class UploadResponse(
    val message: String,
    val urlId: String
)

data class TwistRequest(
    val message: String,
    val twists: List<TwistModel>
)
