package com.grupo18.twister.core.screens.twists.liveTwist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.dp

@Composable
fun PodiumScreen(topPlayers: List<Pair<String, Int>>) {
    // Aseguramos que hay exactamente tres jugadores
    println("Este es el topPlayers: $topPlayers")
    val podiumPlayers = topPlayers.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Gradiente vertical para dar un toque más llamativo
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Título con sombra para resaltar
        Text(
            text = "🏆 Podio 🏆",
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                shadow = Shadow(
                    color = Color.Gray,
                    offset = Offset(4f, 4f),
                    blurRadius = 4f
                )
            )
        )

        // Renderizar el podio con posiciones 2, 1 y 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Segundo lugar
            if (podiumPlayers.size > 1) {
                PodiumPosition(
                    playerName = podiumPlayers[1].first,
                    score = podiumPlayers[1].second,
                    position = 2,
                    height = 140.dp,
                    color = Color(0xFF9E9E9E) // Plata
                )
            }

            // Primer lugar
            if (podiumPlayers.isNotEmpty()) {
                PodiumPosition(
                    playerName = podiumPlayers[0].first,
                    score = podiumPlayers[0].second,
                    position = 1,
                    height = 180.dp,
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
    // Usamos Card para dar un efecto de superficie elevada y esquinas redondeadas
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(90.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Ícono de trofeo decorativo (puedes sustituirlo por otro ícono o imagen)
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = "Ícono de podio",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "${position}º",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = playerName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$score puntos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
