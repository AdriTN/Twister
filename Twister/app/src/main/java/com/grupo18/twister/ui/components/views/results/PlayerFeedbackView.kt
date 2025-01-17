package com.grupo18.twister.ui.components.views.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.random.Random

@Composable
fun PlayerFeedbackView(
    respuestaJugador: String,
    score: Int
) {
    val isCorrect = score > 0

    val correctMessages = listOf(
        "Great job!", "You're on a roll!", "Keep it up!",
        "Awesome answer!", "You did it!", "Fantastic job!", "Well done!"
    )
    val incorrectMessages = listOf(
        "Don't worry, you'll get it next time!",
        "Keep trying!", "You've almost got it!",
        "Stay positive!", "Every mistake is a lesson!", "Try again!"
    )

    val feedbackMessage = if (isCorrect) {
        correctMessages.random()
    } else {
        incorrectMessages.random()
    }

    val backgroundColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Correct" else "Incorrect",
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = feedbackMessage,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your answer: $respuestaJugador",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "+$score",
                    fontSize = 16.sp,
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}