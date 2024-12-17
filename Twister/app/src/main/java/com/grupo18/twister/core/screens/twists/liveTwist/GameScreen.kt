package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.runtime.*
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.RealTimeClient
import com.grupo18.twister.core.models.GameState
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.ResultsResponse
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UserModel
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun GameScreen(
    twist: TwistModel?,
    currentUser: UserModel?,
    pin: String? = null,
    isAdmin: Boolean = false
) {
    var gameStarted by remember { mutableStateOf(false) }
    var currentRoomId by remember { mutableStateOf("") }
    var gameState by remember { mutableStateOf(GameState.SHOWING_QUESTION) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var timerSeconds by remember { mutableStateOf(15) }
    var responses by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    val realTimeClient = remember { RealTimeClient(ApiClient.getSocket()) }

    if (!gameStarted) {
        WaitingRoom(
            onStartGame = { roomId ->
                currentRoomId = roomId
                gameStarted = true
                gameState = GameState.SHOWING_QUESTION
                currentQuestionIndex = 0
            },
            userIsAdmin = isAdmin,
            token = currentUser?.token ?: "",
            pin = pin.toString()
        )
    } else {
        when (gameState) {
            GameState.SHOWING_QUESTION -> {
                currentQuestion = twist?.twistQuestions?.getOrNull(currentQuestionIndex)
                currentQuestion?.let { question ->
                    QuestionView(
                        question = question,
                        timerSeconds = timerSeconds,
                        onTimerTick = { seconds ->
                            timerSeconds = seconds.toInt()
                        },
                        onTimerFinish = {
                            gameState = GameState.SHOWING_RESULTS
                            // Aquí puedes solicitar los resultados al servidor
                            fetchResults(question.id) { result ->
                                responses = result
                            }
                        }
                    )
                }
            }

            GameState.SHOWING_RESULTS -> {
                ResultsView(responses = responses)
                LaunchedEffect(Unit) {
                    // Esperar 3 segundos antes de pasar a la siguiente pregunta
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
                FinalScreen()
            }
        }

        // Escuchar eventos de respuestas en tiempo real
        LaunchedEffect(gameStarted) {
            if (gameStarted) {
                realTimeClient.listenForEvents(roomId = currentRoomId) { event ->
                    when (event.type) {
                        "newAnswer" -> {
                            val answer = event.message.removePrefix("newAnswer: ").trim()
                            responses = responses.toMutableMap().apply {
                                this[answer] = (this[answer] ?: 0) + 1
                            }
                        }
                        // Manejar otros tipos de eventos si es necesario
                    }
                }
            }
        }
    }
}

fun fetchResults(questionId: String, onResultReceived: (Map<String, Int>) -> Unit) {
    val apiService = ApiClient.retrofit.create(ApiService::class.java)
    val token = "Bearer YOUR_AUTH_TOKEN" // Reemplaza con el token real

    apiService.getResultsForQuestion(token, questionId).enqueue(object : Callback<ResultsResponse> {
        override fun onResponse(call: Call<ResultsResponse>, response: Response<ResultsResponse>) {
            if (response.isSuccessful) {
                response.body()?.let { resultsResponse ->
                    onResultReceived(resultsResponse.results)
                }
            } else {
                // Manejar error
            }
        }

        override fun onFailure(call: Call<ResultsResponse>, t: Throwable) {
            // Manejar fallo en la llamada
        }
    })
}
