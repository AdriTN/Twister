package com.grupo18.twister.core.screens.twists

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.viewmodel.QuestionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(
    navController: NavController,
    twistId: String,
    questionViewModel: QuestionViewModel = viewModel()
) {
    var questionText by remember { mutableStateOf("") }
    val answers = remember { mutableStateListOf<AnswerModel>() }
    var answerText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Pregunta") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Pregunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Lista de respuestas
                answers.forEachIndexed { index, answer ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = answer.isCorrect,
                            onCheckedChange = { checked ->
                                answers[index] = answer.copy(isCorrect = checked)
                            }
                        )
                        OutlinedTextField(
                            value = answer.text,
                            onValueChange = { text ->
                                answers[index] = answer.copy(text = text)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            answers.removeAt(index)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Respuesta")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Campo para agregar nueva respuesta
                if (answers.size < 4) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isCorrect,
                            onCheckedChange = { isCorrect = it }
                        )
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            label = { Text("Respuesta") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (answerText.isNotBlank()) {
                                    answers.add(AnswerModel(answerText, isCorrect))
                                    answerText = ""
                                    isCorrect = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar Respuesta")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (questionText.isNotBlank() && answers.isNotEmpty()) {
                            questionViewModel.createQuestion(scope, questionText, answers.toList())
                            navController.navigateUp() // Regresar a la pantalla anterior
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = questionText.isNotBlank() && answers.isNotEmpty()
                ) {
                    Text("Guardar Pregunta")
                }
            }
        }
    )
}
