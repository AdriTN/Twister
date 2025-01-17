package com.grupo18.twister.ui.components.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.network.socket.GameSocketClient
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.ui.components.ColorBlock
import com.grupo18.twister.ui.components.TimeBar
import com.grupo18.twister.ui.components.cards.AnswerText
import com.grupo18.twister.ui.components.cards.QuestionCard
import kotlinx.coroutines.delay

@Composable
fun QuestionView(
    question: QuestionModel,
    timerSeconds: Int,
    isAdmin: Boolean,
    playerName: String,
    pinRoom: String,
    currentQuestion: QuestionModel?,
    // Nuevo parámetro:
    gameSocketClient: GameSocketClient,
    // Callback que se llama cuando el tiempo finaliza
    onTimerFinish: (String) -> Unit
) {
    var timer by remember { mutableStateOf(timerSeconds) }
    val isOver = remember { mutableStateOf(false) }
    val isAnswered = remember { mutableStateOf(false) }

    // Manejo de tiempo restante y progreso
    var remainingSeconds by remember { mutableIntStateOf(timerSeconds) }
    var progress by remember { mutableFloatStateOf(0f) }
    var respuestaJugador by remember { mutableStateOf("") }
    var tiempoJugador by remember { mutableIntStateOf(0) }

    // Lanzar corrutina para el temporizador
    LaunchedEffect(key1 = question.id) {
        // Contamos hacia atrás de timerSeconds a 0
        for (i in timerSeconds downTo 0) {
            remainingSeconds = i
            progress = if (timerSeconds > 0) {
                remainingSeconds.toFloat() / timerSeconds
            } else 0f
            delay(1000L)
        }
        // Al terminar, avisamos al callback
        onTimerFinish(respuestaJugador)
        respuestaJugador = ""
        println("¡El tiempo se ha acabado!")
        isOver.value = true
    }

    // Si el usuario es admin, preguntar periódicamente por respuestas mientras no se acabe el tiempo
    LaunchedEffect(key1 = question.id) {
        if (isAdmin) {
            while (!isOver.value) {
                delay(200)
                println("Obteniendo respuestas para ${question.id} - ${question.question}")
                if (question.id == currentQuestion?.id) {
                    // Llamamos al método de GameSocketClient:
                    gameSocketClient.getAnswers(roomId = pinRoom, questionId = question.id)
                } else {
                    println("Pregunta actual no coincide con la recibida.")
                }
            }
        }
    }

    // Función para manejar la respuesta elegida por el jugador
    fun handleAnswer(answerText: String, answerIndex: Int) {
        respuestaJugador = answerText
        // Calculamos cuánto tiempo le tomó responder
        tiempoJugador = timer - remainingSeconds
        // Enviamos la respuesta a través de GameSocketClient
        gameSocketClient.uploadAnswer(
            answer = "${answerIndex + 1}",
            roomId = pinRoom,
            playerName = playerName,
            time = tiempoJugador,
            questionId = question.id
        )
        isAnswered.value = true
    }

    // Animamos el progreso de la barra de tiempo
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "timeBarProgress"
    )

    // Scroll para evitar que el contenido se corte en pantallas pequeñas
    val scrollState = rememberScrollState()

    // === UI para ADMIN ===
    if (isAdmin) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de tiempo
                TimeBar(
                    progress = animatedProgress,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de la pregunta
                QuestionCard(
                    questionText = question.question,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Estilos de las respuestas
                val answerStyles = listOf(
                    Triple(Color(0xFF4A90E2), RoundedCornerShape(16.dp), Icons.Default.ArrowForward),
                    Triple(Color(0xFFE94E3B), RoundedCornerShape(16.dp), Icons.Default.Circle),
                    Triple(Color(0xFF4CAF50), RoundedCornerShape(16.dp), Icons.Default.Stop),
                    Triple(Color(0xFFFFD700), RoundedCornerShape(16.dp), Icons.Default.Hexagon)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    // Mostramos cada respuesta con su color
                    question.answers.forEachIndexed { index, answer ->
                        val (color, shape, icon) = answerStyles[index % answerStyles.size]
                        AnswerText(
                            answerText = answer.text,
                            color = color,
                            shape = shape,
                            icon = icon,
                            contentDescription = "Answer $index",
                            onClick = {
                                // Si el admin también quiere responder
                                respuestaJugador = answer.text
                                gameSocketClient.uploadAnswer(
                                    answer = "${index + 1}",
                                    roomId = pinRoom,
                                    playerName = playerName,
                                    questionId = question.id,
                                    time = timerSeconds
                                )
                                isAnswered.value = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        // === UI para JUGADOR NO ADMIN ===
        if (!isAnswered.value) {
            val answerStyles = listOf(
                Triple(Color(0xFF4A90E2), RoundedCornerShape(16.dp), Icons.Default.ArrowForward),
                Triple(Color(0xFFE94E3B), RoundedCornerShape(16.dp), Icons.Default.Circle),
                Triple(Color(0xFF4CAF50), RoundedCornerShape(16.dp), Icons.Default.Stop),
                Triple(Color(0xFFFFD700), RoundedCornerShape(16.dp), Icons.Default.Hexagon)
            )

            // Partimos la lista de respuestas en chunks de 2 para mostrarlas en filas
            val respuestasPorFila = question.answers.chunked(2)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                respuestasPorFila.forEach { listaFila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listaFila.forEachIndexed { indexEnFila, respuesta ->
                            val absoluto = question.answers.indexOf(respuesta)
                            val (color, shape, icon) = answerStyles[absoluto % answerStyles.size]

                            ColorBlock(
                                color = color,
                                shape = shape,
                                icon = icon,
                                contentDescription = "Option ${absoluto + 1}",
                                onClick = {
                                    // Manejo de la respuesta
                                    handleAnswer(respuesta.text, absoluto)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Pantalla de "esperando" si el jugador ya respondió
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Waiting for the admin to finalize the question...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = Color(0xFF6200EE),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
