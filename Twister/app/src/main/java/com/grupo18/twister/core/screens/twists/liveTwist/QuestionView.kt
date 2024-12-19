package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.grupo18.twister.core.components.TimeBar
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.screens.twists.liveTwist.components.AnswerText
import com.grupo18.twister.core.screens.twists.liveTwist.components.QuestionCard
import kotlinx.coroutines.delay

@Composable
fun QuestionView(
    question: QuestionModel,
    timerSeconds: Int,
    onTimerTick: (seconds: Long) -> Unit,
    onTimerFinish: () -> Unit,
    onAnswerSelected: (String) -> Unit
) {
    // Estado para manejar los segundos restantes
    var remainingSeconds by remember { mutableIntStateOf(timerSeconds) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Manejar el temporizador
    LaunchedEffect(key1 = question.id) {
        for (i in timerSeconds downTo 0) {
            remainingSeconds = i
            onTimerTick(i.toLong())
            progress = if (timerSeconds > 0) remainingSeconds.toFloat() / timerSeconds else 0f
            delay(1000L)
        }
        onTimerFinish()
    }

    // Estado para animar el progreso de la barra de tiempo
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    // Hacer el contenido desplazable
    val scrollState = rememberScrollState()

    // Usar Scaffold para manejar el padding y evitar solapamientos
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

            // Lista de respuestas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                question.answers.forEach { answer ->
                    AnswerText(
                        answerText = answer.text,
                        onClick = { onAnswerSelected(answer.text) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}