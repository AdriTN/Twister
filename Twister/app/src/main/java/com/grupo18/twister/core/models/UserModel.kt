package com.grupo18.twister.core.models

data class UserModel(
    var token: String,
    val username: String,
    val email: String,
    val password: String,
    val age: Int? = null,
    val avatarUrl: String? = null
)


