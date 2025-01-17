package com.grupo18.twister.ui.components.views.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
@Composable
fun BarItem(
    label: String,
    count: Int,
    maxValue: Int,
    isCorrect: Boolean,
    animatedProgress: Float
) {
    val barColor = if (isCorrect) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    // Ajusta la altura con base en count / maxValue
    val barHeightRatio = (count / maxValue.toFloat()) * animatedProgress
    val barWidth = 40.dp

    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        //modifier = Modifier.weight(1f)
    ) {
        Canvas(
            modifier = Modifier
                .height(200.dp * barHeightRatio)
                .width(barWidth)
        ) {
            if (count > 0) {
                // Barra rellena
                drawRoundRect(
                    color = barColor,
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    size = size
                )
            } else {
                // Barra vacía
                drawRoundRect(
                    color = Color.Transparent,
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    size = size,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Texto de la opción
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium
        )

        // Conteo
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall
        )
    }
}