package com.grupo18.twister.ui.components.dialogs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Confirmar salida",
                style = MaterialTheme.typography.titleLarge, // Cambia a titleLarge si es necesario
                color = MaterialTheme.colorScheme.primary // Cambia a colorScheme para Material3
            )
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Advertencia",
                    tint = MaterialTheme.colorScheme.error, // Cambia a colorScheme
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "¿Estás seguro de que deseas abandonar la sala?",
                    style = MaterialTheme.typography.bodyLarge, // Cambia a bodyMedium si es necesario
                    color = MaterialTheme.colorScheme.onSurface // Cambia a colorScheme
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmExit() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary // Cambia a colorScheme
                )
            ) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary // Cambia a colorScheme
                )
            ) {
                Text("No")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface, // Cambia a containerColor
        shape = RoundedCornerShape(8.dp), // Ajusta según sea necesario
        tonalElevation = 6.dp // Agrega tonalElevation si es necesario
    )
}

