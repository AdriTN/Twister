package com.grupo18.twister.core.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.viewmodel.QuestionViewModel
import com.grupo18.twister.core.viewmodel.TwistViewModel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    twistViewModel: TwistViewModel,
    questionViewModel: QuestionViewModel
) {
    val twists by twistViewModel.twists.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTwist by remember { mutableStateOf<TwistModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Twists") },
                actions = {
                    IconButton(onClick = {
                        selectedTwist = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir Twist")
                    }
                }
            )
        },
        bottomBar = { CustomBottomNavigationBar(navController) },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(twists) { twist ->
                    TwistItem(
                        twist = twist,
                        onEdit = {
                            selectedTwist = twist
                            showDialog = true
                        },
                        onDelete = {
                            twistViewModel.deleteTwist(twist.id)
                        }
                    )
                }
            }

            if (showDialog) {
                EditTwistDialog(
                    initialTwist = selectedTwist,
                    onDismiss = {
                        showDialog = false
                        selectedTwist = null
                    },
                    onSave = { updatedTwist, navigateToQuestions ->
                        if (selectedTwist == null) {
                            // Nuevo Twist
                            val newTwist = twistViewModel.createTwist(
                                title = updatedTwist.title,
                                description = updatedTwist.description,
                                imageUri = updatedTwist.imageUri
                            )
                            showDialog = false
                            selectedTwist = null
                            // Navegación obligatoria a la pantalla de gestión de preguntas para nuevos Twists
                            navController.navigate("manageQuestions/${newTwist.id}")
                        } else {
                            // Editar Twist existente
                            twistViewModel.updateTwist(updatedTwist)
                            showDialog = false
                            selectedTwist = null
                            if (navigateToQuestions) {
                                navController.navigate("manageQuestions/${updatedTwist.id}")
                            }
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun TwistItem(
    twist: TwistModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(twist.title, color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(twist.description, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            twist.imageUri?.let { uri ->
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
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Twist", tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Twist", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
