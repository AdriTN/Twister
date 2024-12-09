package com.grupo18.twister.core.screens.edit

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo18.twister.core.factories.TwistViewModelFactory
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.viewmodel.TwistViewModel
import androidx.compose.ui.graphics.Color
import java.util.UUID
import com.grupo18.twister.core.models.ImageUri

@Composable
fun EditTwistDialog(
    initialTwist: TwistModel? = null,
    onDismiss: () -> Unit,
    onSave: (TwistModel, Boolean) -> Unit
) {
    val viewModelFactory = TwistViewModelFactory(MyApp())
    val twistViewModel: TwistViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTwist?.title ?: "") }
    var description by remember { mutableStateOf(initialTwist?.description ?: "") }

    // Change imageUri to a String to match the expected type
    var imageUri by remember { mutableStateOf<String?>(initialTwist?.imageUri?.uri) } // Assuming imageUri is of type ImageUri in the model

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = ImageUri(it.toString()).uri // Store the URI string
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTwist == null) "Create New Twist" else "Edit Twist") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                imageUri?.let { uri ->
                    // Mostrar la imagen seleccionada y permitir eliminarla
                    ImageWithRemoveButton(uri) { // Use the string representation of the URI
                        imageUri = null
                    }
                }

                // Botón para seleccionar una nueva imagen
                Button(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Select Image")
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
                                imageUri = imageUri?.let { ImageUri(it) } // Convert String back to ImageUri
                            )
                            onSave(updatedTwist, true)
                        }
                    }) {
                        Text("Manage Questions")
                    }
                }

                // Estado para manejar la carga
                var isLoading by remember { mutableStateOf(false) }

                TextButton(onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newTwist = if (initialTwist == null) {
                            TwistModel(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                imageUri = imageUri?.let { ImageUri(it) } // Convert String to ImageUri
                            )
                        } else {
                            initialTwist.copy(
                                title = title,
                                description = description,
                                imageUri = imageUri?.let { ImageUri(it) } // Convert String to ImageUri
                            )
                        }

                        // Cambiar el estado a "cargando"
                        isLoading = true
                        // Si hay imagen, subirla
                        imageUri?.let { uri ->
                            twistViewModel.uploadImage(uri, context.contentResolver) { response ->
                                isLoading = false // Detener la barra de carga al finalizar
                                when {
                                    response.isSuccessful -> {
                                        // Manejar el caso de éxito
                                        onSave(newTwist, false)
                                    }
                                    else -> {
                                        Toast.makeText(context, "Error uploading the image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } ?: run {
                            // Si no hay imagen, simplemente guardar el twist
                            isLoading = false // Detener la barra de carga
                            onSave(newTwist, false)
                        }
                    }
                }) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImageWithRemoveButton(uri: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Cruz roja a la derecha
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(25.dp) // Tamaño del botón
                    .clip(CircleShape) // Forma circular
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(4.dp) // Espaciado interno
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Eliminate image",
                    tint = Color.Red // Cambia el color del icono si es necesario
                )
            }
        }
    }
}
