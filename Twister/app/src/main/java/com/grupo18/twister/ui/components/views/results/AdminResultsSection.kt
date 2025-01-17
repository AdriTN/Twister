package com.grupo18.twister.ui.components.views.results

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo18.twister.models.network.domain.OpcionRespuesta
import com.grupo18.twister.models.network.domain.RespuestaJugador

/**
 * Sección de resultados para el administrador:
 * - Muestra una gráfica de barras con las respuestas.
 * - Muestra una lista detallada (DetailCard) de cada opción.
 * - Botón "Next" para pasar de pregunta.
 */
@Composable
fun AdminResultsSection(
    responses: List<RespuestaJugador>,
    options: List<OpcionRespuesta>,
    onNextQuestionClick: () -> Unit
) {
    // Calcular cuántas respuestas obtuvo cada opción
    val responseCounts: Map<String, Int> = remember(responses, options) {
        options.associate { option ->
            option.text to responses.count {
                it.answer.equals(option.text, ignoreCase = true)
            }
        }
    }

    // Determinar valor máximo para escalar las barras
    val maxValue = responseCounts.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    // Animación para las barras
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000), label = "progressBarAdmin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Results",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Gráfica de Barras
        AdminBarChart(
            responseCounts = responseCounts,
            options = options,
            maxValue = maxValue,
            animatedProgress = animatedProgress
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Detalles de Respuestas
        Text(
            text = "Response Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            content = {
                items(options) { option ->
                    val responseCount = responseCounts[option.text] ?: 0
                    DetailCard(option = option, responseCount = responseCount)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Next
        Button(
            onClick = onNextQuestionClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}