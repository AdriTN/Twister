package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.grupo18.twister.core.models.OpcionRespuesta
import com.grupo18.twister.core.models.RespuestaJugador

@Composable
fun ResultsView(
    responses: List<RespuestaJugador>,
    options: List<OpcionRespuesta>,
    isAdmin: Boolean,
    isCorrect: Boolean,
    respuestaJugador: String,
    onNextQuestionClick: () -> Unit,
) {
    // Procesar las respuestas para obtener el conteo por opción
    val responseCounts: Map<String, Int> = remember(responses, options) {
        options.associate { option ->
            option.text to responses.count { it.answer.equals(option.text, ignoreCase = true) }
        }
    }

    // Determinar el valor máximo para escalar las barras
    val maxValue = responseCounts.values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val barWidth = 40.dp
    val spacing = 16.dp

    // Animación para las barras
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    if (isAdmin) {
        // Vista para Administradores: Mostrar gráfica de barras y lista de respuestas
        AdminResultsView(
            responseCounts = responseCounts,
            options = options,
            maxValue = maxValue,
            barWidth = barWidth,
            spacing = spacing,
            animatedProgress = animatedProgress,
            onNextQuestionClick = onNextQuestionClick
        )
    } else {
        PlayerFeedbackView(
            respuestaJugador = respuestaJugador,
            isCorrect = isCorrect,
        )
    }
}

@Composable
fun AdminResultsView(
    responseCounts: Map<String, Int>,
    options: List<OpcionRespuesta>,
    maxValue: Int,
    barWidth: Dp,
    spacing: Dp,
    animatedProgress: Float,
    onNextQuestionClick: () -> Unit
) {
    // Identificar la opción correcta
    val correctOption = options.find { it.isCorrect }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Resultados",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Gráfica de Barras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
        ) {
            responseCounts.forEach { (optionText, count) ->
                val isCorrect = optionText == correctOption?.text
                val barColor =
                    if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Barra de Respuesta
                    Canvas(
                        modifier = Modifier
                            .height(200.dp * (count / maxValue.toFloat()) * animatedProgress)
                            .width(barWidth)
                    ) {
                        if (count > 0) {
                            // Dibujar barra rellena
                            drawRoundRect(
                                color = barColor,
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                size = size
                            )
                        } else {
                            // Dibujar barra con borde para indicar vacía
                            drawRoundRect(
                                color = Color.Transparent,
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                size = size,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Texto de la Opción
                    Text(
                        text = optionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        modifier = Modifier.width(barWidth + 20.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Conteo de Respuestas
                    Text(
                        text = count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Respuestas con Detalles en Cuadrícula
        Text(
            text = "Detalle de Respuestas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Cuadrícula de Detalles de Respuestas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(options) { option ->
                DetailCard(option = option, responseCount = responseCounts[option.text] ?: 0)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para Siguiente Pregunta
        Button(
            onClick = onNextQuestionClick,
            enabled = true, // El botón está habilitado para administradores
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espaciado entre el texto y el icono
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Question",
                    tint = MaterialTheme.colorScheme.onPrimary // Color fijo para mejor contraste
                )
            }
        }
    }
}

    @Composable
    fun DetailCard(option: OpcionRespuesta, responseCount: Int) {
        // Identificar si esta opción es la correcta
        val isCorrect = option.isCorrect

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrect) Color(0xFF7CC57E).copy(alpha = 1f) else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de Correcto/Incorrecto
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (isCorrect) "Correcto" else "Incorrecto",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Texto de la Opción
                    Text(
                        text = option.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Conteo de Respuestas
                Text(
                    text = "Respuestas: $responseCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

@Composable
fun PlayerFeedbackView(
    respuestaJugador: String,
    isCorrect: Boolean,
) {
    // Mensajes de ánimo
    val correctMessages = listOf(
        "¡Excelente trabajo!",
        "¡Estás en racha!",
        "¡Sigue así!",
        "¡Respuesta asombrosa!",
        "¡Lo lograste!",
        "¡Trabajo fantástico!",
        "¡Bien hecho!"
    )

    val incorrectMessages = listOf(
        "No te preocupes, ¡lo lograrás la próxima vez!",
        "¡Sigue intentando!",
        "¡Casi lo consigues!",
        "¡Tú puedes!",
        "¡Mantén una actitud positiva!",
        "¡Cada error es una lección!",
        "¡Inténtalo de nuevo, tú puedes!"
    )

    val feedbackMessage = if (isCorrect) {
        correctMessages[Random.nextInt(correctMessages.size)]
    } else {
        incorrectMessages[Random.nextInt(incorrectMessages.size)]
    }

    // Color de fondo basado en la respuesta
    val backgroundColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta para el contenido central
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de éxito o error
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Correcto" else "Incorrecto",
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Mensaje principal
                Text(
                    text = if (isCorrect) "¡Correcto!" else "¡Incorrecto!",
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Mensaje de ánimo o celebración
                Text(
                    text = feedbackMessage,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Mostrar la respuesta del jugador
                Text(
                    text = "Tu respuesta: $respuestaJugador",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
