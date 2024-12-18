package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.models.QuestionModel
import kotlinx.coroutines.delay

@Composable
fun QuestionView(
    question: QuestionModel,
    timerSeconds: Int,
    onTimerTick: (seconds: Long) -> Unit,
    onTimerFinish: () -> Unit
) {
    // Manejar el temporizador
    LaunchedEffect(key1 = question.id) {
        for (i in timerSeconds downTo 0) {
            onTimerTick(i.toLong())
            delay(1000L)
        }
        onTimerFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.question,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        question.answers.forEach { answer ->
            Button(
                onClick = { /* Implementar lógica de respuesta si es necesario */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = answer.text, fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Tiempo restante: $timerSeconds segundos",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}