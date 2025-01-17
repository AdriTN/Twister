package com.grupo18.twister.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon

@Composable
fun ColorBlock(
    color: Color,
    shape: Shape,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(150.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = color), // Mantener color de fondo
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}