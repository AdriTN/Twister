package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.api.RealTimeClient
import com.grupo18.twister.core.components.ColorBlock
import com.grupo18.twister.core.models.OpcionRespuesta
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.RespuestaJugador
import kotlinx.coroutines.delay
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun QuestionView(
    question: QuestionModel,
    timerSeconds: Int,
    isAdmin: Boolean,
    playerName: String,
    onTimerTick: (seconds: Long) -> Unit,
    realTimeClient: RealTimeClient,
    pinRoom: String,
    onTimerFinish: () -> Unit
) {
    val isOver = remember { mutableStateOf(false) }
    val isAnswered = remember { mutableStateOf(false) }
    // Manejar el temporizador
    LaunchedEffect(key1 = question.id) {
        for (i in timerSeconds downTo 0) {
            onTimerTick(i.toLong())
            delay(1000L)
        }
        //onTimerFinish()
        //isOver.value = true
    }

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            while (!isOver.value) {
                delay(200)
                realTimeClient.getAnswers(roomId = pinRoom, questionId = question.id)
            }
        }
    }
    if (isAdmin){
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
                            realTimeClient.uploadAnswer(answer = "4", roomId = pinRoom, playerName = playerName, question.id)
                            isAnswered.value = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        else {
            //TODO: Mostrar respuesta
        }

    }
}

