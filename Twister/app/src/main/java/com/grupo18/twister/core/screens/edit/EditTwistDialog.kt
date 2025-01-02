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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.grupo18.twister.core.components.PublicPrivateSwitch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun EditTwistDialog(
    initialTwist: TwistModel? = null,
    onDismiss: () -> Unit,
    onSave: (TwistModel, Boolean) -> Unit
) {
    val viewModelFactory = TwistViewModelFactory(MyApp())
    val twistViewModel: TwistViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    val app = context.applicationContext as MyApp
    var title by remember { mutableStateOf(initialTwist?.title ?: "") }
    var description by remember { mutableStateOf(initialTwist?.description ?: "") }
    val scope = rememberCoroutineScope()
    val lastimageUri = initialTwist?.imageUri

    var imageUri by remember { mutableStateOf<String?>(initialTwist?.imageUri) }
    var imageToRemove by remember { mutableStateOf(false) }

    // Estado para la privacidad
    var isPublic by remember { mutableStateOf(initialTwist?.isPublic == true) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newUri ->
            val tempDir = File(context.cacheDir, "tempImages")
            if (!tempDir.exists()) {
                tempDir.mkdir()
            }

            // Crear archivo temporal
            val tempFile = File(tempDir, "${System.currentTimeMillis()}.jpg")

            // Copiar contenido
            context.contentResolver.openInputStream(newUri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val tempFilePath = tempFile.absolutePath

            // Verificar si es la misma imagen
            val isSame = twistViewModel.isSameImage("${context.filesDir}/images/${lastimageUri}", tempFilePath, context)
            if (!isSame) {
                imageUri = tempFilePath
            } else {
                Toast.makeText(context, "Debes seleccionar una imagen diferente a la actual", Toast.LENGTH_SHORT).show()
            }
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
                    val localFilePath = "${context.filesDir}/images/${uri}"

                    // Verifica si el archivo existe
                    val imageFile = File(localFilePath)
                    if (imageFile.exists()) {
                        // Si el archivo existe, se muestra la imagen
                        ImageWithRemoveButton(uri = localFilePath) {
                            imageToRemove = true
                            imageUri = null
                        }

                    } else {
                        // Si el archivo no existe, intenta usar la imagen temporal
                        val tempImagePath = "${context.cacheDir}/tempImages/${imageFile.name}"
                        val tempImageFile = File(tempImagePath)
                        if (tempImageFile.exists()) {
                            ImageWithRemoveButton(tempImagePath) {
                                imageToRemove = true
                                imageUri = null
                            }
                        } else {
                            println("No se encontró ninguna imagen en el path especificado.")
                        }
                    }
                }

                // Botón para seleccionar una nueva imagen
                Button(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Select Image")
                }

                Spacer(modifier = Modifier.height(8.dp))

                PublicPrivateSwitch(
                    isPublic = isPublic,
                    onCheckedChange = { newValue ->
                        isPublic = newValue
                    }
                )

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
                                imageUri = imageUri,
                                isPublic = isPublic // Añadimos el estado de privacidad
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
                                imageUri = imageUri,
                                isPublic = isPublic // Añadimos aquí la propiedad de privacidad
                            )
                        } else {
                            initialTwist.copy(
                                title = title,
                                description = description,
                                isPublic = isPublic // Actualizamos el estado de privacidad
                            )
                        }

                        // Manejo de eliminación de la imagen
                        if (imageToRemove) {
                            println("Se va a eliminar la imagen")
                            twistViewModel.deleteImageFromTwist(
                                token = "Bearer ${app.currentUser.value?.token ?: ""}", // Cambio aquí
                                Twist = newTwist,
                                scope = scope,
                                context = context
                            )
                            imageUri = null // Resetea el estado de la imagen
                        }

                        isLoading = true
                        println("Aqui imageUri es $imageUri")

                        imageUri?.let {
                            // Asegúrate de que no sea la misma imagen antes de intentar cargarla
                            if (it != lastimageUri) {
                                twistViewModel.uploadImage(
                                    context,
                                    "Bearer ${app.currentUser.value?.token ?: ""}", // Cambio aquí
                                    imageUri!!,
                                    context.contentResolver
                                ) { response ->
                                    isLoading = false
                                    when {
                                        response.isSuccessful -> {
                                            val uploadResponse = response.body()
                                            uploadResponse?.let { upload ->
                                                newTwist.imageUri = upload.urlId
                                                println("Se ha cambiado la imagen por ${newTwist.imageUri}")
                                                onSave(newTwist, false)
                                            } ?: run {
                                                Toast.makeText(
                                                    context,
                                                    "Error parsing the response",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Error uploading the image",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                isLoading = false
                                onSave(newTwist, false) // No se sube imagen nueva
                            }
                        } ?: run {
                            isLoading = false
                            onSave(newTwist, false) // Si no hay imageUri, solo guarda sin imagen
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

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = { onRemove() },
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Eliminate image",
                    tint = Color.Red
                )
            }
        }
    }
}
