package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@Composable
fun ResultsView(responses: Map<String, Int>, isAdmin: Boolean) {
    val maxValue = responses.values.maxOrNull() ?: 1
    val barWidth = 40.dp
    val spacing = 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resultados",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            responses.forEach { (key, value) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(
                        modifier = Modifier
                            .height((value / maxValue.toFloat()) * 200.dp)
                            .width(barWidth)
                    ) {
                        drawRect(
                            color = Color.Blue,
                            size = size
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = key)
                    Text(text = value.toString())
                }
            }
        }
    }
}