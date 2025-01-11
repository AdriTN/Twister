package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PodiumScreen(topPlayers: List<Pair<String, Int>>) {
    // Aseguramos que hay exactamente tres jugadores
    println("Este es el topPlayers: $topPlayers")
    val podiumPlayers = topPlayers.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "🏆 Podio 🏆",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Renderizar el podio con posiciones 2, 1 y 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // Segundo lugar
            if (podiumPlayers.size > 1) {
                PodiumPosition(
                    playerName = podiumPlayers[1].first,
                    score = podiumPlayers[1].second,
                    position = 2,
                    height = 150.dp,
                    color = Color(0xFF9E9E9E) // Plata
                )
            }

            // Primer lugar
            if (podiumPlayers.isNotEmpty()) {
                PodiumPosition(
                    playerName = podiumPlayers[0].first,
                    score = podiumPlayers[0].second,
                    position = 1,
                    height = 200.dp,
                    color = Color(0xFFFFD700) // Oro
                )
            }

            // Tercer lugar
            if (podiumPlayers.size > 2) {
                PodiumPosition(
                    playerName = podiumPlayers[2].first,
                    score = podiumPlayers[2].second,
                    position = 3,
                    height = 120.dp,
                    color = Color(0xFFCD7F32) // Bronce
                )
            }
        }
    }
}

@Composable
fun PodiumPosition(
    playerName: String,
    score: Int,
    position: Int,
    height: Dp,
    color: Color
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .height(height)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(color)
        )

        Text(
            text = "${position}º",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = playerName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "$score puntos",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
