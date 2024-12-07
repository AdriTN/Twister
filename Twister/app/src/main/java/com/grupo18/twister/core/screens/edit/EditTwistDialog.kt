package com.grupo18.twister.core.screens.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.grupo18.twister.core.models.TwistModel

@Composable
fun EditTwistDialog(
    initialTwist: TwistModel?,
    onDismiss: () -> Unit,
    onSave: (TwistModel, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialTwist?.title ?: "") }
    var description by remember { mutableStateOf(initialTwist?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(initialTwist?.imageUri) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTwist == null) "Crear Nuevo Twist" else "Editar Twist") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Seleccionar Imagen")
                }
            }
        },
        confirmButton = {
            Row {
                // Si es un Twist existente, se muestra el botón "Gestionar Preguntas"
                if (initialTwist != null) {
                    TextButton(onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            val updatedTwist = initialTwist.copy(
                                title = title,
                                description = description,
                                imageUri = imageUri
                            )
                            // true indica que se navegará a la pantalla de preguntas después de guardar
                            onSave(updatedTwist, true)
                        }
                    }) {
                        Text("Gestionar Preguntas")
                    }
                }

                TextButton(onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val updatedTwist = if (initialTwist == null) {
                            TwistModel(
                                title = title,
                                description = description,
                                imageUri = imageUri
                            )
                        } else {
                            initialTwist.copy(
                                title = title,
                                description = description,
                                imageUri = imageUri
                            )
                        }
                        // false: no navegar directamente a la pantalla de preguntas
                        onSave(updatedTwist, false)
                    }
                }) {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
