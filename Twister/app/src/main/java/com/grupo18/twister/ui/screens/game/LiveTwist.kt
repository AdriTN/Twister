package com.grupo18.twister.ui.screens.game

import androidx.compose.runtime.*
import com.grupo18.twister.models.game.GameState
import com.grupo18.twister.models.game.TwistModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavController
import com.grupo18.twister.core.network.socket.GameSocketClient
import com.grupo18.twister.core.network.socket.SocketManager
import com.grupo18.twister.models.common.Event
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.models.network.domain.OpcionRespuesta
import com.grupo18.twister.models.network.domain.RespuestaJugador
import com.grupo18.twister.models.network.events.AnswerProvidedEvent
import com.grupo18.twister.models.network.events.CorrectAnswerEvent
import com.grupo18.twister.models.network.events.GameStateEvent
import com.grupo18.twister.models.network.events.NextQuestionEvent
import com.grupo18.twister.models.network.events.QuestionTimeoutEvent
import com.grupo18.twister.navigation.Routes
import com.grupo18.twister.ui.components.views.QuestionView
import com.grupo18.twister.ui.components.views.results.ResultsView
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LiveTwist(
    twist: TwistModel?,
    isAdmin: Boolean,
    currentRoomId: String,
    playerName: String,
    navController: NavController,
    roomQuestions: SnapshotStateList<QuestionModel>
) {
    // Estados del juego
    var gameState by remember { mutableStateOf(GameState.SHOWING_QUESTION) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var timerSeconds by remember { mutableIntStateOf(5) }
    var score by remember { mutableIntStateOf(0) }
    var lastScore by remember { mutableIntStateOf(0) }
    var opcionesRespuestas by remember { mutableStateOf(listOf<OpcionRespuesta>()) }
    var respuestasJugador by remember { mutableStateOf(listOf<RespuestaJugador>()) }
    var respuestaJugador by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var topPlayers by remember { mutableStateOf(listOf<Pair<String, Int>>()) }

    // Inicialización de GameSocketClient (en lugar de RealTimeClient)
    // Asegúrate de haber llamado a SocketManager.connect() en alguna parte previa de la app
    val gameSocketClient = remember {
        GameSocketClient(SocketManager.getSocket())
    }

    // Actualización de la pregunta actual basada en el índice
    LaunchedEffect(currentQuestionIndex, twist) {
        currentQuestion = if (isAdmin) {
            twist?.twistQuestions?.getOrNull(currentQuestionIndex)
        } else {
            roomQuestions.getOrNull(currentQuestionIndex)
        }
        println("Pregunta actual: $currentQuestion y roomQuestions: $roomQuestions")
    }

    // Renderizado basado en el estado del juego
    when (gameState) {
        GameState.SHOWING_QUESTION -> {
            currentQuestion?.let { question ->
                QuestionView(
                    question = question,
                    timerSeconds = timerSeconds,
                    isAdmin = isAdmin,
                    playerName = playerName,
                    pinRoom = currentRoomId,
                    currentQuestion = currentQuestion,
                    // Usamos GameSocketClient en lugar de RealTimeClient
                    gameSocketClient = gameSocketClient,
                    onTimerFinish = { respuesta ->
                        if (!isAdmin) {
                            respuestaJugador = respuesta
                            // Llamamos a getCorrectAnswer en GameSocketClient
                            gameSocketClient.getCorrectAnswer(
                                roomId = currentRoomId,
                                questionId = question.id,
                                playerName = playerName
                            )
                        }
                        gameState = GameState.SHOWING_RESULTS
                    }
                )
            }
        }

        GameState.SHOWING_RESULTS -> {
            ResultsView(
                responses = respuestasJugador,
                options = opcionesRespuestas,
                isAdmin = isAdmin,
                score = lastScore,
                respuestaJugador = respuestaJugador,
                onNextQuestionClick = {
                    // Llamamos a nextQuestion en GameSocketClient
                    gameSocketClient.nextQuestion(
                        roomId = currentRoomId,
                        questionId = currentQuestionIndex.toString()
                    )
                    if (isAdmin) {
                        if (currentQuestionIndex < (twist?.twistQuestions?.size ?: 0) - 1) {
                            currentQuestionIndex++
                            timerSeconds = 5
                            gameState = GameState.SHOWING_QUESTION
                        } else {
                            gameState = GameState.FINALIZED
                        }
                    }
                }
            )
        }

        GameState.FINALIZED -> {
            if (isAdmin) {
                // Solicitamos top players y lanzamos evento game over
                gameSocketClient.getTopPlayers(roomId = currentRoomId)
                gameSocketClient.gameOverEvent(roomId = currentRoomId)
            }
            LaunchedEffect(Unit) {
                delay(500)
                println("Top players en launched effect: $topPlayers")
                navController.navigate(
                    Routes.FINAL_SCREEN
                        .replace("{topPlayers}", topPlayers.toString())
                        .replace("{isAdmin}", isAdmin.toString())
                )
            }
        }
    }

    // Listener de eventos del juego: Reemplaza listenForEventsInGame en RealTimeClient
    // por listenForInGameEvents en GameSocketClient
    LaunchedEffect(currentRoomId) {
        if (currentRoomId.isNotEmpty()) {
            gameSocketClient.listenForInGameEvents { event ->
                when (event.type) {
                    "GAME_STATE" -> handleGameState(event) { newState ->
                        gameState = newState
                    }

                    "NEXT_QUESTION" -> {
                        // Actualizar la pregunta actual
                        currentQuestionIndex++
                        gameState = GameState.SHOWING_QUESTION
                    }

                    "GAME_OVER" -> {
                        gameState = GameState.FINALIZED
                        println("Juego finalizado.")
                    }

                    "TOP_SCORES" -> {
                        try {
                            println("Event message recibido: ${event.message}")
                            // Ejemplo simplificado de parseo
                            val parts = event.message.split(":").map { it.trim() }
                            if (parts.size == 2) {
                                val topScores = mapOf(parts[0] to parts[1].toInt())
                                println("Puntajes máximos:")
                                topScores.forEach { (playerName, score) ->
                                    println("Jugador: $playerName, Puntaje: $score")
                                }
                                topPlayers = topScores.toList()
                            } else {
                                println("Formato de mensaje no válido: ${event.message}")
                            }
                        } catch (e: SerializationException) {
                            println("Error al procesar TOP_SCORES: ${e.localizedMessage}")
                        }
                    }

                    "QUESTION_TIMEOUT" -> handleQuestionTimeout(event) { questionId ->
                        gameState = GameState.SHOWING_RESULTS
                    }

                    "CORRECT_ANSWER" -> {
                        val answerEvent = Json.decodeFromString<CorrectAnswerEvent>(event.message)
                        isCorrect = (answerEvent.correctAnswer.text == respuestaJugador)
                        score = answerEvent.score
                        println(
                            "Respuesta correcta: $isCorrect con $respuestaJugador. Valor real: " +
                                    answerEvent.correctAnswer.text
                        )
                    }

                    "ANSWER_SENT" -> {
                        val answerEvent = event.id.toInt()
                        println("El score obtenido es: $answerEvent")
                        lastScore = answerEvent
                    }

                    "ANSWERS" -> {
                        try {
                            println("Evento ANSWERS recibido: ${event.message}")
                            val jsonElement = Json.parseToJsonElement(event.message)
                            if (jsonElement is JsonArray) {
                                val nuevasOpcionesRespuestas = mutableListOf<OpcionRespuesta>()
                                val nuevasRespuestasJugador = mutableListOf<RespuestaJugador>()

                                jsonElement.forEach { element ->
                                    val jsonObject = element.jsonObject
                                    when {
                                        "isCorrect" in jsonObject -> {
                                            val isCorrect =
                                                jsonObject["isCorrect"]?.jsonPrimitive?.booleanOrNull == true
                                            val text =
                                                jsonObject["text"]?.jsonPrimitive?.contentOrNull ?: ""
                                            nuevasOpcionesRespuestas.add(
                                                OpcionRespuesta(
                                                    isCorrect,
                                                    text
                                                )
                                            )
                                        }

                                        "playerName" in jsonObject -> {
                                            val playerName =
                                                jsonObject["playerName"]?.jsonPrimitive?.contentOrNull ?: ""
                                            val answer =
                                                jsonObject["answer"]?.jsonPrimitive?.contentOrNull ?: ""
                                            nuevasRespuestasJugador.add(
                                                RespuestaJugador(
                                                    playerName,
                                                    answer
                                                )
                                            )
                                        }
                                    }
                                }
                                // Actualizar los estados reactivos
                                opcionesRespuestas = nuevasOpcionesRespuestas
                                respuestasJugador = nuevasRespuestasJugador

                                println("Opciones Respuestas:")
                                opcionesRespuestas.forEach { println(it) }
                                println("Respuestas Jugador:")
                                respuestasJugador.forEach { println(it) }
                            } else {
                                println("El mensaje recibido no es un array JSON válido")
                            }
                            println("Tiempo expirado para la pregunta")
                        } catch (e: SerializationException) {
                            println("Error al procesar el mensaje ANSWERS: ${e.localizedMessage}")
                        }
                    }

                    else -> {
                        println("Evento desconocido: ${event.type}")
                    }
                }
            }
        }
    }
}

// Funciones auxiliares para manejar cada tipo de evento
private fun handleGameState(event: Event, onGameStateChange: (GameState) -> Unit) {
    try {
        val gameStateEvent = Json.decodeFromString<GameStateEvent>(
            event.message.removePrefix("GAME_STATE: ")
        )
        val newState = when (gameStateEvent.state) {
            "SHOWING_QUESTION" -> GameState.SHOWING_QUESTION
            "SHOWING_RESULTS" -> GameState.SHOWING_RESULTS
            "fFINALIZED" -> GameState.FINALIZED
            else -> GameState.SHOWING_QUESTION
        }
        onGameStateChange(newState)
    } catch (e: Exception) {
        println("Error al manejar GAME_STATE: ${e.localizedMessage}")
    }
}

private fun handleNextQuestion(event: Event, onNextQuestion: (String, String) -> Unit) {
    try {
        val nextQuestionEvent = Json.decodeFromString<NextQuestionEvent>(
            event.message.removePrefix("NEXT_QUESTION: ")
        )
        onNextQuestion(nextQuestionEvent.questionId, nextQuestionEvent.questionText)
    } catch (e: Exception) {
        println("Error al manejar NEXT_QUESTION: ${e.localizedMessage}")
    }
}

private fun handleAnswerProvided(event: Event, onAnswer: (String, String) -> Unit) {
    try {
        val answerEvent = Json.decodeFromString<AnswerProvidedEvent>(
            event.message.removePrefix("ANSWER_PROVIDED: ")
        )
        onAnswer(answerEvent.playerId, answerEvent.answer)
    } catch (e: Exception) {
        println("Error al manejar ANSWER_PROVIDED: ${e.localizedMessage}")
    }
}

private fun handleQuestionTimeout(event: Event, onTimeout: (String) -> Unit) {
    try {
        val timeoutEvent = Json.decodeFromString<QuestionTimeoutEvent>(
            event.message.removePrefix("QUESTION_TIMEOUT: ")
        )
        onTimeout(timeoutEvent.questionId)
    } catch (e: Exception) {
        println("Error al manejar QUESTION_TIMEOUT: ${e.localizedMessage}")
    }
}
