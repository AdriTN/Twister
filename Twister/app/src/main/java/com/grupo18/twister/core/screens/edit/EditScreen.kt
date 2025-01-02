package com.grupo18.twister.core.screens.edit

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.ImageService
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.viewmodel.TwistViewModel
import java.io.File

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    twistViewModel: TwistViewModel,
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyApp
    val twists by twistViewModel.twists.collectAsState()
    var isLoading by remember { mutableStateOf(true) } // Estado de carga
    val coroutineScope = rememberCoroutineScope()
    var user = app.currentUser.value ?: return
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTwist by remember { mutableStateOf<TwistModel?>(null) }

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var twistToDelete by remember { mutableStateOf<TwistModel?>(null) }



    // Popup para usuarios anónimos
    var showAnonymousAlert by remember { mutableStateOf(false) }

    val currentUser = app.currentUser.value

    // Efecto que carga los twists desde el servidor al iniciar la pantalla
    LaunchedEffect(Unit) {
        twistViewModel.clearTwists()
        user = app.currentUser.value ?: return@LaunchedEffect
        val user = currentUser ?: return@LaunchedEffect
        twistViewModel.loadTwists(user.token, coroutineScope, context = context) { loading ->
            isLoading = loading
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Twists") },
                actions = {
                    IconButton(onClick = {
                        // Si el usuario es anónimo, no permitir crear Twists directamente
                        if (currentUser?.isAnonymous == true) {
                            showAnonymousAlert = true
                        } else {
                            selectedTwist = null
                            showDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Twist")
                    }
                }
            )
        },
        bottomBar = { CustomBottomNavigationBar(navController) },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (twists.isEmpty()) {
                        Text(
                            text = "No twists available. Press the + button to create a new one.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(padding)
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Mostrar lista de twists
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(twists) { twist ->
                                TwistItem(
                                    twist = twist,
                                    onEdit = {
                                        selectedTwist = twist
                                        showDialog = true
                                    },
                                    onDeleteRequested = {
                                        twistToDelete = it // Set the twist to be deleted
                                        showDeleteConfirmationDialog = true // Show the confirmation dialog
                                    }
                                )
                            }
                        }

                    }
                }

                // Mostrar diálogo si está habilitado (para crear/editar twist)
                if (showDialog) {
                    EditTwistDialog(
                        initialTwist = selectedTwist,
                        onDismiss = {
                            showDialog = false
                            selectedTwist = null
                        },
                        onSave = { updatedTwist, navigateToQuestions ->
                            if (selectedTwist == null) {
                                val newTwist = twistViewModel.createTwist(
                                    title = updatedTwist.title,
                                    description = updatedTwist.description,
                                    imageUri = updatedTwist.imageUri.toString(),
                                    isPublic = updatedTwist.isPublic
                                )
                                println("El nuevo imageUri es ESTE ${newTwist.imageUri}")
                                showDialog = false
                                selectedTwist = null
                                val twistJson = Gson().toJson(newTwist)
                                navController.navigate("manageQuestions/${twistJson}")
                            } else {
                                twistViewModel.updateTwist(user.token, updatedTwist, scope, context)
                                showDialog = false
                                selectedTwist = null
                                if (navigateToQuestions) {
                                    val twistJson = Gson().toJson(updatedTwist)
                                    navController.navigate("manageQuestions/${twistJson}")
                                }
                            }
                        }
                    )
                }

                // Mostrar popup si el usuario es anónimo y trata de crear un Twist
                if (showAnonymousAlert) {
                    AlertDialog(
                        onDismissRequest = { showAnonymousAlert = false },
                        title = { Text("Login Required") },
                        text = {
                            Text(
                                "You are currently browsing as a guest. Please log in to create new twists and access all features."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showAnonymousAlert = false
                                navController.navigate("auth") // Navegar a login
                            }) {
                                Text("Go to Login")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAnonymousAlert = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                if (showDeleteConfirmationDialog && twistToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmationDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete, // Icono de eliminación
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el icono y el texto
                                Text("Confirm Deletion", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                            }
                        },
                        text = {
                            Column {
                                Text("Are you sure you want to delete this twist?", style = TextStyle(fontSize = 16.sp))
                                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre el texto
                                // Muestra la información del twist
                                Text("Title: ${twistToDelete?.title}", style = TextStyle(fontSize = 14.sp))
                                Text("Description: ${twistToDelete?.description}", style = TextStyle(fontSize = 14.sp))
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    twistToDelete?.let { twist ->
                                        twistViewModel.deleteTwist(user.token, twist.id, coroutineScope, context)
                                    }
                                    showDeleteConfirmationDialog = false
                                    twistToDelete = null
                                }
                            ) {
                                Text("Yes", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmationDialog = false
                                    twistToDelete = null
                                }
                            ) {
                                Text("No", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    )
                }

            }
        }
    )
}

@Composable
fun TwistItem(
    twist: TwistModel,
    onEdit: () -> Unit,
    onDeleteRequested: (TwistModel) -> Unit
) {
    val context = LocalContext.current
    val repository = ImageService(ApiClient.retrofit.create(ApiService::class.java))
    val uriString = twist.imageUri
    val dominantColorInt = try {
        if (!uriString.isNullOrEmpty() && uriString.length > 6) {
            repository.extractDominantColorFromUri(uriString, context)
        } else {
            Color.Gray.toArgb()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Devolver un color predeterminado en caso de error
        Color.Gray.toArgb()
    }


    val dominantColor = Color(dominantColorInt)

    // Función para determinar el color del texto (iconos) basado en el brillo
    fun getContrastColor(color: Color): Color {
        val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
        return if (luminance < 0.5) Color.White else Color.Black
    }

    // Determina el color del icono según el color dominante
    val iconColor = getContrastColor(dominantColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { // Asegúrate de establecer una altura
            val localFilePath = "${context.filesDir}/images/${twist.imageUri}"
            val localFile = File(localFilePath)
            if (localFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                println("Aqui el bitmap es $bitmap")
            } else {
                println("El archivo no existe en la ruta $localFile")
            }

            Image(
                painter = rememberAsyncImagePainter(localFile),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            twist.imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
                // Degradado sobre la imagen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            } ?: run {
                // Si no hay imagen, aplica el color de fondo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(dominantColor)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(twist.title, color = iconColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(twist.description, color = iconColor.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Twist",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    IconButton(
                        onClick = { onDeleteRequested(twist) },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminate Twist",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
