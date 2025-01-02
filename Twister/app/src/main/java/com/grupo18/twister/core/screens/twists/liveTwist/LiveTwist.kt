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
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.models.Event
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerProvidedEvent
import com.grupo18.twister.core.models.GameOverEvent
import com.grupo18.twister.core.models.GameStateEvent
import com.grupo18.twister.core.models.NextQuestionEvent
import com.grupo18.twister.core.models.OpcionRespuesta
import com.grupo18.twister.core.models.QuestionTimeoutEvent
import com.grupo18.twister.core.models.RespuestaJugador
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.contains
import kotlin.collections.forEach

@Composable
fun LiveTwist(twist: TwistModel?, isAdmin: Boolean, currentRoomId: String, playerName: String, navController: NavController, roomQuestions: SnapshotStateList<QuestionModel>) {
    // Estados del juego
    var gameState by remember { mutableStateOf(GameState.SHOWING_QUESTION) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var timerSeconds by remember { mutableIntStateOf(7) }
    var opcionesRespuestas by remember { mutableStateOf(listOf<OpcionRespuesta>()) }
    var respuestasJugador by remember { mutableStateOf(listOf<RespuestaJugador>()) }
    var respuestaJugador by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }

    // Inicialización del RealTimeClient
    val realTimeClient = remember { RealTimeClient(ApiClient.getSocket()) }

    // Actualización de la pregunta actual basada en el índice
    LaunchedEffect(currentQuestionIndex, twist) {
        currentQuestion = if (isAdmin){
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
                    realTimeClient = realTimeClient,
                    playerName = playerName,
                    pinRoom = currentRoomId,
                    onTimerTick = { seconds ->
                        timerSeconds = seconds.toInt()
                    },
                    currentQuestion = currentQuestion,
                    onTimerFinish = { respuesta ->
                        if (!isAdmin){
                            respuestaJugador = respuesta
                            realTimeClient.getCorrectAnswer(roomId = currentRoomId, questionId = question.id)
                        }
                        gameState = GameState.SHOWING_RESULTS
                    },
                )
            }
        }

        GameState.SHOWING_RESULTS -> {
            ResultsView(responses = respuestasJugador, options = opcionesRespuestas, isAdmin = isAdmin, isCorrect = isCorrect, respuestaJugador = respuestaJugador, onNextQuestionClick = {
                realTimeClient.nextQuestion(roomId = currentRoomId, questionId = currentQuestionIndex.toString())
                if (isAdmin){
                    if (currentQuestionIndex < (twist?.twistQuestions?.size ?: 0) - 1) {
                        currentQuestionIndex++
                        gameState = GameState.SHOWING_QUESTION
                        timerSeconds = 15
                    } else {
                        gameState = GameState.FINALIZED
                    }
                }
            })
        }

        GameState.FINALIZED -> {
            if (isAdmin) {
                realTimeClient.gameOverEvent(roomId = currentRoomId)
            }
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
                    "NEXT_QUESTION" -> {
                        // Actualizar la pregunta actual
                        currentQuestionIndex++
                        // Aquí podrías buscar y actualizar `currentQuestion` si es necesario
                        gameState = GameState.SHOWING_QUESTION
                        timerSeconds = 15
                        println("Nueva pregunta")
                    }
//                    "GAME_OVER" -> handleGameOver(event, onGameOver = { winnerId, finalScores ->
//                        // Mostrar pantalla final o manejar el estado finalizado
//                        gameState = GameState.FINALIZED
//                        println("Juego finalizado. Ganador: $winnerId")
//                    })

                    "GAME_OVER" -> {
                        gameState = GameState.FINALIZED
                        println("Juego finalizado.")
                    }
                    "QUESTION_TIMEOUT" -> handleQuestionTimeout(event, onTimeout = { questionId ->
                        // Manejar el timeout de la pregunta actual
                        gameState = GameState.SHOWING_RESULTS
                    })
                    "CORRECT_ANSWER" -> {
                        // Manejar el timeout de la pregunta actual
                        //TODO ACEPTAR MAS DE UNA RESPUESTA CORRECTA
                        val answerEvent = Json.decodeFromString<AnswerModel>(event.message)
                        isCorrect = answerEvent.text == respuestaJugador
                        println("Respuesta correcta: $isCorrect con $respuestaJugador y answervalor: ${answerEvent.text}")
                    }
                    // Manejar evento ANSWERS al expirar tiempo de pregunta
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
                                                    jsonObject["text"]?.jsonPrimitive?.contentOrNull
                                                        ?: ""
                                                nuevasOpcionesRespuestas.add(
                                                    OpcionRespuesta(
                                                        isCorrect,
                                                        text
                                                    )
                                                )
                                            }

                                            "playerName" in jsonObject -> {
                                                val playerName =
                                                    jsonObject["playerName"]?.jsonPrimitive?.contentOrNull
                                                        ?: ""
                                                val answer =
                                                    jsonObject["answer"]?.jsonPrimitive?.contentOrNull
                                                        ?: ""
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

                                    // Imprimir los resultados para verificar
                                    println("Opciones Respuestas:")
                                    opcionesRespuestas.forEach { println(it) }

                                    println("Respuestas Jugador:")
                                    respuestasJugador.forEach { println(it) }
                                } else {
                                    println("El mensaje recibido no es un array JSON válido")
                                }
                            } catch (e: SerializationException) {
                                println("Error al procesar el mensaje ANSWERS: ${e.localizedMessage}")
                            }
                            println("Tiempo expirado para la pregunta")
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
        //TODO onGameOver(gameOverEvent.winnerId, gameOverEvent.roomId)
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
