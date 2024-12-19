    package com.grupo18.twister.core.screens.twists.liveTwist

    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateListOf
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.runtime.snapshots.SnapshotStateList
    import androidx.navigation.NavController
    import com.grupo18.twister.core.models.QuestionModel
    import com.grupo18.twister.core.models.TwistModel
    import com.grupo18.twister.core.models.UserModel

    @Composable
    fun GameScreen(twist: TwistModel?, currentUser: UserModel?, pin: String? = null, isAdmin: Boolean = false, navController: NavController) {
        var gameStarted by remember { mutableStateOf(false) }
        var currentRoomId by remember { mutableStateOf("") }
        var playerName by remember { mutableStateOf("") }
        var roomQuestions: SnapshotStateList<QuestionModel> = remember { mutableStateListOf() }

        if (!gameStarted) {
            if (currentUser != null) {
                WaitingRoom(
                    onStartGame = { roomId, nplayerName, questions ->
                        currentRoomId = roomId
                        gameStarted = true
                        playerName = nplayerName
                        roomQuestions.clear() // Limpia la lista existente
                        roomQuestions.addAll(questions) // Agrega las nuevas preguntas
                    },
                    userIsAdmin = isAdmin,
                    token = currentUser.token,
                    pin = pin.toString(),
                    twist = twist,
                    navController = navController
                )
            }
        } else {
            print("Room ID dasda: $roomQuestions")
            LiveTwist(twist = twist, isAdmin = isAdmin, currentRoomId = currentRoomId, navController = navController, roomQuestions = roomQuestions, playerName = playerName)
        }
    }
