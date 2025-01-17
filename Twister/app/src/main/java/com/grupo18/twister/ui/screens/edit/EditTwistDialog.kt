package com.grupo18.twister.ui.screens.edit

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.ui.components.PublicPrivateSwitch
import com.grupo18.twister.viewmodels.factories.TwistViewModelFactory
import com.grupo18.twister.viewmodels.screens.TwistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun EditTwistDialog(
    initialTwist: TwistModel? = null,
    onDismiss: () -> Unit,
    onSave: (TwistModel, Boolean) -> Unit
) {
    // Obtenemos el contexto y la instancia de MyApp desde el Application
    val context = LocalContext.current
    val app = context.applicationContext as MyApp

    // Instanciamos el ViewModel con su fábrica
    val twistViewModel: TwistViewModel = viewModel(
        factory = TwistViewModelFactory(app)
    )

    // Definimos un scope para corrutinas
    val scope = rememberCoroutineScope()

    // Variables de estado
    var title by remember { mutableStateOf(initialTwist?.title.orEmpty()) }
    var description by remember { mutableStateOf(initialTwist?.description.orEmpty()) }
    val lastImageUri = initialTwist?.imageUri

    // Manejo de la imagen
    var imageUri by remember { mutableStateOf(lastImageUri) }
    var imageToRemove by remember { mutableStateOf(false) }

    // Estado de privacidad
    var isPublic by remember { mutableStateOf(initialTwist?.isPublic ?: false) }

    // Launcher para seleccionar la imagen desde la galería
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { newUri: Uri? ->
        newUri?.let { safeUri ->
            val tempDir = File(context.cacheDir, "tempImages").apply {
                if (!exists()) mkdir()
            }
            // Crear archivo temporal
            val tempFile = File(tempDir, "${System.currentTimeMillis()}.jpg")

            // Copiar el contenido de la Uri al archivo temporal
            context.contentResolver.openInputStream(safeUri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val tempFilePath = tempFile.absolutePath
            // Verificamos si la imagen es la misma que antes
            val isSameImage = twistViewModel.isSameImage(
                currentImageUri = "${context.filesDir}/images/$lastImageUri",
                newImageUri = tempFilePath,
                context = context
            )
            if (!isSameImage) {
                imageUri = tempFilePath
            } else {
                Toast.makeText(
                    context,
                    "You must select a different image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTwist == null) "Create New Twist" else "Edit Twist"
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Imagen
                imageUri?.let { currentUri ->
                    val localFilePath = "${context.filesDir}/images/$currentUri"
                    val imageFile = File(localFilePath)
                    if (imageFile.exists()) {
                        // Imagen original
                        ImageWithRemoveButton(
                            uri = localFilePath,
                            onRemove = {
                                imageToRemove = true
                                imageUri = null
                            }
                        )
                    } else {
                        // Comprobamos imagen temporal
                        val tempImagePath = "${context.cacheDir}/tempImages/${imageFile.name}"
                        val tempImageFile = File(tempImagePath)
                        if (tempImageFile.exists()) {
                            ImageWithRemoveButton(
                                uri = tempImagePath,
                                onRemove = {
                                    imageToRemove = true
                                    imageUri = null
                                }
                            )
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

                // Switch público/privado
                PublicPrivateSwitch(
                    isPublic = isPublic,
                    onCheckedChange = { isPublic = it }
                )
            }
        },
        confirmButton = {
            Row {
                // Botón "Manage Questions" (sólo si existe un Twist previo)
                if (initialTwist != null) {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                val updatedTwist = initialTwist.copy(
                                    title = title,
                                    description = description,
                                    imageUri = imageUri,
                                    isPublic = isPublic
                                )
                                onSave(updatedTwist, true)
                            }
                        }
                    ) {
                        Text("Manage Questions")
                    }
                }

                // Estado de carga
                var isLoading by remember { mutableStateOf(false) }

                // Botón "Save"
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            val newTwist = if (initialTwist == null) {
                                // Crear Twist nuevo
                                TwistModel(
                                    id = UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    imageUri = imageUri,
                                    isPublic = isPublic
                                )
                            } else {
                                // Editar Twist existente
                                initialTwist.copy(
                                    title = title,
                                    description = description,
                                    isPublic = isPublic
                                )
                            }

                            // Si marcamos que eliminamos la imagen
                            if (imageToRemove) {
                                println("Se va a eliminar la imagen")
                                twistViewModel.deleteImageFromTwist(
                                    token = "${app.currentUser.value?.token.orEmpty()}",
                                    Twist = newTwist,
                                    scope = scope,
                                    context = context
                                )
                                imageUri = null
                            }

                            isLoading = true
                            println("imageUri actual: $imageUri")

                            imageUri?.let { safeUri ->
                                // Si es distinta de la anterior
                                if (safeUri != lastImageUri) {
                                    twistViewModel.uploadImage(
                                        context = context,
                                        token = "${app.currentUser.value?.token.orEmpty()}",
                                        imageUri = safeUri,
                                        contentResolver = context.contentResolver
                                    ) { response ->
                                        isLoading = false
                                        if (response.isSuccessful) {
                                            response.body()?.let { upload ->
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
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error uploading the image",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    // Mantenemos la misma imagen
                                    isLoading = false
                                    onSave(newTwist, false)
                                }
                            } ?: run {
                                // No hay imagen => se guarda sin subir
                                isLoading = false
                                onSave(newTwist, false)
                            }
                        }
                    }
                ) {
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

/**
 * Muestra la imagen y un botón en la esquina superior derecha
 * para eliminarla.
 */
@Composable
fun ImageWithRemoveButton(
    uri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        // Imagen
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Botón de eliminar
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove image",
                    tint = Color.Red
                )
            }
        }
    }
}
