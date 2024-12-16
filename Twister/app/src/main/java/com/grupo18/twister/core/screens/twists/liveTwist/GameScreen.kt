package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UserModel
import com.grupo18.twister.core.screens.twists.LiveTwist

@Composable
fun GameScreen(twist: TwistModel?, currentUser: UserModel?) {
    var gameStarted by remember { mutableStateOf(false) }
    var currentRoomId by remember { mutableStateOf("") }
    if (!gameStarted) {
        WaitingRoom(onStartGame = { roomId ->
            currentRoomId = roomId // Almacena el roomId al iniciar el juego
            gameStarted = true
            // Puedes realizar más acciones con currentRoomId aquí
        }, token = currentUser?.token ?: "")
    } else {
        LiveTwist(twist, token = currentUser?.token.toString(), roomId = currentRoomId)
    }
}
