// Archivo: EditQuestionsScreen.kt
package com.grupo18.twister.core.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.grupo18.twister.core.components.CustomBottomNavigationBar
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.viewmodel.TwistViewModel
import com.grupo18.twister.core.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    questionViewModel: QuestionViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as MyApp
    val currentUser by app.getUser().collectAsState(initial = null)
    val authToken = currentUser?.token

    if (authToken == null) {
        Text("Necesitas iniciar sesión para gestionar las preguntas.")
        return
    }

    LaunchedEffect(authToken) {
        questionViewModel.setAuthToken(authToken)
    }

    val questions by questionViewModel.questions.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<QuestionModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Preguntas") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir Pregunta")
                    }
                }
            )
        },
        bottomBar = { CustomBottomNavigationBar(navController) },
        content = { padding ->
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay preguntas. Toca + para añadir una.", fontSize = 16.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(questions) { question ->
                        QuestionItem(
                            question = question,
                            onEdit = {
                                selectedQuestion = question
                                showDialog = true
                            },
                            onDelete = {
                                questionViewModel.deleteQuestion(question.id)
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                QuestionDialog(
                    question = selectedQuestion,
                    onDismiss = {
                        showDialog = false
                        selectedQuestion = null
                    },
                    onSave = { questionText, answers ->
                        if (selectedQuestion == null) {
                            questionViewModel.createQuestion(questionText, answers)
                        } else {
                            questionViewModel.editQuestion(selectedQuestion!!.id, questionText, answers)
                        }
                        showDialog = false
                        selectedQuestion = null
                    }
                )
            }
        }
    )
}

@Composable
fun QuestionItem(
    question: QuestionModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question.question, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            question.answers.forEach { answer ->
                Text(
                    text = if (answer.isCorrect) "✔ ${answer.text}" else answer.text,
                    color = if (answer.isCorrect) Color.Yellow else Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Pregunta", tint = Color.White)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Pregunta", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun QuestionDialog(
    question: QuestionModel? = null,
    onDismiss: () -> Unit,
    onSave: (String, List<AnswerModel>) -> Unit
) {
    var questionText by remember { mutableStateOf(question?.question ?: "") }
    val answers = remember { mutableStateListOf<AnswerModel>() }

    // Inicializar la lista de respuestas cuando la pregunta cambia
    LaunchedEffect(question) {
        answers.clear()
        answers.addAll(question?.answers ?: emptyList())
    }

    var answerText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (question == null) "Añadir Nueva Pregunta" else "Editar Pregunta") },
        text = {
            Column {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Pregunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar las respuestas actuales
                answers.forEach { answer ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = answer.isCorrect,
                            onCheckedChange = null, // Solo lectura
                            enabled = false
                        )
                        Text(
                            text = answer.text,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            answers.remove(answer)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Respuesta")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campos para añadir una nueva respuesta
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Respuesta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCorrect,
                        onCheckedChange = { isCorrect = it }
                    )
                    Text("Es la respuesta correcta")
                }
                Button(
                    onClick = {
                        if (answerText.isNotBlank()) {
                            answers.add(AnswerModel(text = answerText, isCorrect = isCorrect))
                            answerText = ""
                            isCorrect = false
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Añadir Respuesta")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (questionText.isNotBlank() && answers.isNotEmpty() && answers.any { it.isCorrect }) {
                        onSave(questionText, answers.toList()) // Pasamos una copia inmutable de la lista
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
