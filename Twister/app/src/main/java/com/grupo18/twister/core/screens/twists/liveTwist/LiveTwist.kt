package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.runtime.*
import com.grupo18.twister.core.models.GameState
import com.grupo18.twister.core.models.TwistModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavController
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.RealTimeClient
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerProvidedEvent
import com.grupo18.twister.core.models.GameOverEvent
import com.grupo18.twister.core.models.GameStateEvent
import com.grupo18.twister.core.models.NextQuestionEvent
import com.grupo18.twister.core.models.QuestionTimeoutEvent
import kotlinx.serialization.json.Json

@Composable
fun LiveTwist(twist: TwistModel?, isAdmin: Boolean, currentRoomId: String, navController: NavController, roomQuestions: SnapshotStateList<QuestionModel>) {
    // Estados del juego
    var gameState by remember { mutableStateOf(GameState.SHOWING_QUESTION) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var timerSeconds by remember { mutableIntStateOf(15) }
    var responses by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Inicialización del RealTimeClient
    val realTimeClient = remember { RealTimeClient(ApiClient.getSocket()) }
    val socket = ApiClient.getSocket()

    // Actualización de la pregunta actual basada en el índice
    LaunchedEffect(currentQuestionIndex, twist) {
        if (isAdmin){
            currentQuestion = twist?.twistQuestions?.getOrNull(currentQuestionIndex)
        } else {
            currentQuestion = roomQuestions.getOrNull(currentQuestionIndex)
        }
    }

    // Renderizado basado en el estado del juego
    when (gameState) {
        GameState.SHOWING_QUESTION -> {
            currentQuestion?.let { question ->
                QuestionView(
                    question = question,
                    timerSeconds = timerSeconds,
                    onTimerTick = { seconds ->
                        timerSeconds = seconds.toInt()
                    },
                    onTimerFinish = {
                        gameState = GameState.SHOWING_RESULTS
                        // Opcional: Emitir evento de fin de pregunta si es necesario
                    }
                )
            }
        }

        GameState.SHOWING_RESULTS -> {
            ResultsView(responses = responses)
            LaunchedEffect(Unit) {
                delay(3000)
                if (currentQuestionIndex < (twist?.twistQuestions?.size ?: 0) - 1) {
                    currentQuestionIndex++
                    gameState = GameState.SHOWING_QUESTION
                    timerSeconds = 15
                    responses = emptyMap() // Reiniciar respuestas para la nueva pregunta
                } else {
                    gameState = GameState.FINALIZED
                }
            }
        }

        GameState.FINALIZED -> {
            FinalScreen(navController = navController)
        }
    }

    // Listener de eventos del juego
    LaunchedEffect(currentRoomId) {
        if (currentRoomId.isNotEmpty()) {
            realTimeClient.listenForEventsInGame(currentRoomId) { event ->
                when (event.type) {
                    "GAME_STATE" -> handleGameState(event, onGameStateChange = { newState ->
                        gameState = newState
                    })
                    "NEXT_QUESTION" -> handleNextQuestion(event, onNextQuestion = { newQuestionId, questionText ->
                        // Actualizar la pregunta actual
                        currentQuestionIndex++
                        // Aquí podrías buscar y actualizar `currentQuestion` si es necesario
                        gameState = GameState.SHOWING_QUESTION
                        timerSeconds = 15
                        responses = emptyMap()
                        println("Nueva pregunta: $questionText")
                    })
                    "ANSWER_PROVIDED" -> handleAnswerProvided(event, onAnswer = { playerId, answer ->
                        // Actualizar las respuestas recibidas
                        responses = responses.toMutableMap().apply {
                            this[playerId] = (this[playerId] ?: 0) + 1
                        }
                        println("Respuesta recibida de $playerId: $answer")
                    })
                    "GAME_OVER" -> handleGameOver(event, onGameOver = { winnerId, finalScores ->
                        // Mostrar pantalla final o manejar el estado finalizado
                        gameState = GameState.FINALIZED
                        println("Juego finalizado. Ganador: $winnerId")
                    })
                    "QUESTION_TIMEOUT" -> handleQuestionTimeout(event, onTimeout = { questionId ->
                        // Manejar el timeout de la pregunta actual
                        gameState = GameState.SHOWING_RESULTS
                        println("Tiempo expirado para la pregunta: $questionId")
                    })
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
    // Parsear el evento GAME_STATE
    try {
        val gameStateEvent = Json.decodeFromString<GameStateEvent>(event.message.removePrefix("GAME_STATE: "))
        val newState = when (gameStateEvent.state) {
            "SHOWING_QUESTION" -> GameState.SHOWING_QUESTION
            "SHOWING_RESULTS" -> GameState.SHOWING_RESULTS
            "FINALIZED" -> GameState.FINALIZED
            else -> GameState.SHOWING_QUESTION
        }
        onGameStateChange(newState)
    } catch (e: Exception) {
        println("Error al manejar GAME_STATE: ${e.localizedMessage}")
    }
}

private fun handleNextQuestion(event: Event, onNextQuestion: (String, String) -> Unit) {
    // Parsear el evento NEXT_QUESTION
    try {
        val nextQuestionEvent = Json.decodeFromString<NextQuestionEvent>(event.message.removePrefix("NEXT_QUESTION: "))
        onNextQuestion(nextQuestionEvent.questionId, nextQuestionEvent.questionText)
    } catch (e: Exception) {
        println("Error al manejar NEXT_QUESTION: ${e.localizedMessage}")
    }
}

private fun handleAnswerProvided(event: Event, onAnswer: (String, String) -> Unit) {
    // Parsear el evento ANSWER_PROVIDED
    try {
        val answerEvent = Json.decodeFromString<AnswerProvidedEvent>(event.message.removePrefix("ANSWER_PROVIDED: "))
        onAnswer(answerEvent.playerId, answerEvent.answer)
    } catch (e: Exception) {
        println("Error al manejar ANSWER_PROVIDED: ${e.localizedMessage}")
    }
}

private fun handleGameOver(event: Event, onGameOver: (String, Map<String, Int>) -> Unit) {
    // Parsear el evento GAME_OVER
    try {
        val gameOverEvent = Json.decodeFromString<GameOverEvent>(event.message.removePrefix("GAME_OVER: "))
        onGameOver(gameOverEvent.winnerId, gameOverEvent.finalScores)
    } catch (e: Exception) {
        println("Error al manejar GAME_OVER: ${e.localizedMessage}")
    }
}

private fun handleQuestionTimeout(event: Event, onTimeout: (String) -> Unit) {
    // Parsear el evento QUESTION_TIMEOUT
    try {
        val timeoutEvent = Json.decodeFromString<QuestionTimeoutEvent>(event.message.removePrefix("QUESTION_TIMEOUT: "))
        onTimeout(timeoutEvent.questionId)
    } catch (e: Exception) {
        println("Error al manejar QUESTION_TIMEOUT: ${e.localizedMessage}")
    }
}