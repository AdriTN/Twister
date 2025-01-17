package com.grupo18.twister.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
fun PodiumScreen(topPlayers: List<Pair<String, Int>> = emptyList(), isAdmin: Boolean?, onNavigateToHome: () -> Unit) {
    println("Este es el topPlayers: $topPlayers")

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAdmin == true) {
            // Interfaz para el administrador (la actual)
            val podiumPlayers = topPlayers.take(3)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (podiumPlayers.size > 1) {
                        PodiumPosition(
                            playerName = podiumPlayers[1].first,
                            score = podiumPlayers[1].second,
                            position = 2,
                            height = 140.dp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    if (podiumPlayers.isNotEmpty()) {
                        PodiumPosition(
                            playerName = podiumPlayers[0].first,
                            score = podiumPlayers[0].second,
                            position = 1,
                            height = 180.dp,
                            color = Color(0xFFFFD700)
                        )
                    }

                    if (podiumPlayers.size > 2) {
                        PodiumPosition(
                            playerName = podiumPlayers[2].first,
                            score = podiumPlayers[2].second,
                            position = 3,
                            height = 120.dp,
                            color = Color(0xFFCD7F32)
                        )
                    }
                }
            }
        } else {
            // Interfaz para los jugadores no administradores
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Your Score",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (topPlayers.isNotEmpty()) {
                            val playerScore = topPlayers.firstOrNull()?.second ?: 0
                            Text(
                                text = "$playerScore points",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "No data available",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Botón en la parte inferior, ligeramente más arriba
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp), // Ajuste para colocarlo más arriba
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home Icon",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Go to Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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