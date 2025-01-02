package com.grupo18.twister.core.components

import android.R.attr.checked
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PublicPrivateSwitch(isPublic: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp) // Espaciado vertical
    ) {
        // Texto para "Private"
        Text(
            text = "Private",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (!isPublic) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(end = 16.dp) // Espaciado entre el texto y el switch
        )

        // Switch
        Switch(
            checked = isPublic,
            onCheckedChange = onCheckedChange, // Llama al callback al cambiar
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            thumbContent = {
                Icon(
                    imageVector = if (isPublic) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = if (isPublic) "Public" else "Private",
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        )

        // Texto para "Public"
        Text(
            text = "Public",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isPublic) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(start = 16.dp) // Espaciado entre el switch y el texto
        )
    }
}
