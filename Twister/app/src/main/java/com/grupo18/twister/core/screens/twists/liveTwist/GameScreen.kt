package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UserModel

@Composable
fun GameScreen(twist: TwistModel?, currentUser: UserModel?, pin: String? = null, isAdmin: Boolean = false, navController: NavController) {
    var gameStarted by remember { mutableStateOf(false) }
    var currentRoomId by remember { mutableStateOf("") }
    if (!gameStarted) {
        if (currentUser != null) {
            WaitingRoom(
                onStartGame = { roomId ->
                    currentRoomId = roomId // Almacena el roomId al iniciar el juego
                    gameStarted = true
                },
                userIsAdmin = isAdmin,
                token = currentUser.token,
                pin = pin.toString(),
                navController = navController
            )
        }
    } else {
        LiveTwist(twist, token = currentUser?.token.toString(), roomId = currentRoomId)
    }
}
