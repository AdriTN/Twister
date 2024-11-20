package com.grupo18.twister.core.screens.navigation

import kotlinx.serialization.Serializable

@Serializable
object Welcome

@Serializable
object Auth

@Serializable
data class LoginData(val username: String, val password: String)

@Serializable
object Home

@Serializable
object Twists

@Serializable
object Search

@Serializable
object Edit

@Serializable
object Settings
