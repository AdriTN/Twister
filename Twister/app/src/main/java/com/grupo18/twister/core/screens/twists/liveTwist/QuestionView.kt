package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.api.RealTimeClient
import com.grupo18.twister.core.components.ColorBlock
import com.grupo18.twister.core.components.TimeBar
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.screens.twists.liveTwist.components.AnswerText
import com.grupo18.twister.core.screens.twists.liveTwist.components.QuestionCard
import kotlinx.coroutines.delay

@Composable
fun QuestionView(
    question: QuestionModel,
    timerSeconds: Int,
    isAdmin: Boolean,
    playerName: String,
    onTimerTick: (seconds: Long) -> Unit,
    realTimeClient: RealTimeClient,
    pinRoom: String,
    onTimerFinish: (String) -> Unit,
) {
    val isOver = remember { mutableStateOf(false) }
    val isAnswered = remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(timerSeconds) }
    var progress by remember { mutableFloatStateOf(0f) }
    var respuestaJugador by remember { mutableStateOf("") }

    // Manejar el temporizador
    LaunchedEffect(key1 = question.id) {
        for (i in timerSeconds downTo 0) {
            remainingSeconds = i
            onTimerTick(i.toLong())
            progress = if (timerSeconds > 0) remainingSeconds.toFloat() / timerSeconds else 0f
            delay(1000L)
        }
        onTimerFinish(respuestaJugador)
        isOver.value = true
    }

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            while (!isOver.value) {
                delay(200)
                realTimeClient.getAnswers(roomId = pinRoom, questionId = question.id)
            }
        }
    }

    // Estado para animar el progreso de la barra de tiempo
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    )

    // Hacer el contenido desplazable
    val scrollState = rememberScrollState()

    if (isAdmin){
        // Definir las figuras en orden
        val figuras: List<ImageVector> = listOf(
            Icons.Default.ArrowForward, // Flecha
            Icons.Default.Circle,        // Círculo
            Icons.Default.Stop,          // Cuadrado (usando Stop como placeholder)
            Icons.Default.Hexagon        // Hexágono (asegúrate de tener este ícono o usa uno personalizado)
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding() // Evitar solapamiento con barras del sistema
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState), // Hacer el contenido desplazable
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de tiempo
                TimeBar(
                    progress = animatedProgress,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de pregunta
                QuestionCard(
                    questionText = question.question,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lista de respuestas en dos por fila
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    // Agrupar las respuestas de dos en dos
                    val pairedAnswers = question.answers.chunked(2)

                    pairedAnswers.forEachIndexed { rowIndex, answerPair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            answerPair.forEachIndexed { columnIndex, answer ->
                                // Obtener la figura correspondiente
                                val figura = if (rowIndex * 2 + columnIndex < figuras.size) {
                                    figuras[rowIndex * 2 + columnIndex]
                                } else {
                                    Icons.Default.Circle // Figura por defecto si excede la lista
                                }

                                // Mostrar la respuesta con su figura
                                AnswerWithFigure(
                                    answerText = answer.text,
                                    figura = figura,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    onClick = { /* Acción para administrador si es necesario */ }
                                )
                            }

                            // Si hay una respuesta impar, agregar un espacio vacío
                            if (answerPair.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        if (!isAnswered.value){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorBlock(
                        color = Color(0xFF4A90E2),
                        shape = RoundedCornerShape(16.dp),
                        icon = Icons.Default.ArrowForward,
                        contentDescription = "Arrow",
                        onClick = {
                            respuestaJugador = question.answers[0].text
                            realTimeClient.uploadAnswer(answer = "1", roomId = pinRoom, playerName = playerName, question.id)
                            isAnswered.value = true
                        }
                    )
                    ColorBlock(
                        color = Color(0xFFE94E3B),
                        shape = RoundedCornerShape(16.dp),
                        icon = Icons.Default.Circle,
                        contentDescription = "Circle",
                        onClick = {
                            respuestaJugador = question.answers[1].text
                            realTimeClient.uploadAnswer(answer = "2", roomId = pinRoom, playerName = playerName, question.id)
                            isAnswered.value = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorBlock(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(16.dp),
                        icon = Icons.Default.Stop,
                        contentDescription = "Square",
                        onClick = {
                            respuestaJugador = question.answers[2].text
                            realTimeClient.uploadAnswer(answer = "3", roomId = pinRoom, playerName = playerName, question.id)
                            isAnswered.value = true
                        }
                    )
                    ColorBlock(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(16.dp),
                        icon = Icons.Default.Hexagon,
                        contentDescription = "Hexagon",
                        onClick = {
                            respuestaJugador = question.answers[3].text
                            realTimeClient.uploadAnswer(answer = "4", roomId = pinRoom, playerName = playerName, question.id)
                            isAnswered.value = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        else {
            // Implementación del mensaje de espera
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Esperando a que el administrador termine la pregunta...",
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

@Composable
fun AnswerWithFigure(
    answerText: String,
    figura: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = figura,
            contentDescription = "Figura de $answerText",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = answerText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
