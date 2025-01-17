package com.grupo18.twister.models.common

data class UserModel(
    var token: String,
    val username: String,
    val email: String,
    val password: String,
    val age: Int? = null,
    val avatarUrl: String? = null,
    val achievements: List<String>? = null,
    val isAnonymous: Boolean = false
)


