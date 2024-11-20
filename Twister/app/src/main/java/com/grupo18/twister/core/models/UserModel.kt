package com.grupo18.twister.core.models

data class UserModel(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String? = null,
    val age: Int? = null,
    val avatarUrl: String? = null
)
