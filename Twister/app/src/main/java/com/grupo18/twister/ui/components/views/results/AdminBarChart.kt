package com.grupo18.twister.ui.components.views.results

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo18.twister.models.network.domain.OpcionRespuesta

@Composable
fun AdminBarChart(
    responseCounts: Map<String, Int>,
    options: List<OpcionRespuesta>,
    maxValue: Int,
    animatedProgress: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        // Identificar la opción correcta
        val correctOption = options.find { it.isCorrect }

        responseCounts.forEach { (optionText, count) ->
            val isCorrect = optionText == correctOption?.text

            BarItem(
                label = optionText,
                count = count,
                maxValue = maxValue,
                isCorrect = isCorrect,
                animatedProgress = animatedProgress
            )
        }
    }
}