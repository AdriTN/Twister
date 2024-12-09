package com.grupo18.twister.core.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.viewmodel.QuestionViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.grupo18.twister.core.models.ImageUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageQuestionsScreen(
    navController: NavController,
    twistId: String,
    token: String,
    title: String,
    description: String,
    imageUri: ImageUri? = null,
    questionViewModel: QuestionViewModel
) {
    val questions by questionViewModel.getQuestionsForTwist(twistId).collectAsState()

    var showQuestionDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var showError by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Preguntas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (questions.size < 25) {
                        IconButton(onClick = {
                            selectedQuestion = null
                            showQuestionDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Pregunta")
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                if (questions.isEmpty()) {
                    Text("No hay preguntas. Añade al menos una con una respuesta correcta antes de guardar.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(questions) { question ->
                            QuestionCard(
                                question = question,
                                onEdit = {
                                    selectedQuestion = question
                                    showQuestionDialog = true
                                },
                                onDelete = {
                                    questionViewModel.deleteQuestion(twistId, question.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (showError) {
                    Text("Debes tener al menos una pregunta con una respuesta correcta para guardar.", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        // Verificar que haya al menos una pregunta con respuesta correcta
                        val valid = questionViewModel.hasAtLeastOneCorrectAnswer(twistId)
                        if (!valid) {
                            showError = true
                        } else {
                            questionViewModel.saveChanges(token, twistId, title, description, imageUri)
                            questionViewModel.reloadDataFromApi(twistId)
                            navController.popBackStack() // Guardar y volver atrás
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }

            if (showQuestionDialog) {
                QuestionDialog(
                    question = selectedQuestion,
                    onDismiss = {
                        showQuestionDialog = false
                        selectedQuestion = null
                    },
                    onSave = { questionText, answers ->
                        if (selectedQuestion == null) {
                            questionViewModel.createQuestion(twistId, questionText, answers)
                        } else {
                            questionViewModel.editQuestion(twistId, selectedQuestion!!.id, questionText, answers)
                        }
                        showQuestionDialog = false
                        selectedQuestion = null
                    },
                    twistId = twistId,
                )
            }
        }
    )
}

@Composable
fun QuestionCard(
    question: QuestionModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                question.question,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            question.answers.forEach { answer ->
                Text(
                    text = if (answer.isCorrect) "✔ ${answer.text}" else answer.text,
                    color = if (answer.isCorrect) Color.Yellow else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Pregunta", tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Pregunta", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
