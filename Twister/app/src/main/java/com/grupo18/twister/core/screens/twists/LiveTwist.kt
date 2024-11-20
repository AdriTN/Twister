package com.grupo18.twister.core.screens.twists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.grupo18.twister.core.components.ColorBlock

@Composable
fun LiveTwist() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
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
                onClick = { /* Acción al presionar */ }
            )
            ColorBlock(
                color = Color(0xFFE94E3B),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Circle,
                contentDescription = "Circle",
                onClick = { /* Acción al presionar */ }
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
                onClick = { /* Acción al presionar */ }
            )
            ColorBlock(
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(16.dp),
                icon = Icons.Default.Hexagon,
                contentDescription = "Hexagon",
                onClick = { /* Acción al presionar */ }
            )
        }
    }
}
