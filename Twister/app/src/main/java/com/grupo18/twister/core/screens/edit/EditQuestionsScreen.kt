package com.grupo18.twister.core.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.viewmodel.QuestionViewModel
import com.grupo18.twister.core.viewmodel.TwistViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionsScreen(
    navController: NavController,
    twistId: String,
    questionViewModel: QuestionViewModel = viewModel(),
    twistViewModel: TwistViewModel = viewModel()
) {
    val twist = twistViewModel.getTwistById(twistId)
    if (twist == null) {
        Text("Element not found")
        return
    }

    val questions by questionViewModel.getQuestionsForTwist(twistId).collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<QuestionModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preguntas de '${twist.title}'") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Question")
                    }
                }
            )
        },
        content = { padding ->
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("There are no questions. Tap + to add one.", fontSize = 16.sp, color = Color.Gray)
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
                                questionViewModel.deleteQuestion(twistId, question.id)
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                QuestionDialog(
                    twistId = twistId,
                    question = selectedQuestion,
                    onDismiss = {
                        showDialog = false
                        selectedQuestion = null
                    },
                    onSave = { questionText, answers ->
                        if (selectedQuestion == null) {
                            questionViewModel.createQuestion(twistId, questionText, answers)
                        } else {
                            questionViewModel.editQuestion(twistId, selectedQuestion!!.id, questionText, answers)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                question.question,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            question.answers.forEach { answer ->
                Text(
                    text = if (answer.isCorrect) "✔ ${answer.text}" else answer.text,
                    color = if (answer.isCorrect) Color.Yellow else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Question", tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminate Question", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun QuestionDialog(
    twistId: String, // También puedes pasar twistId si lo necesitas
    question: QuestionModel? = null,
    onDismiss: () -> Unit,
    onSave: (String, List<AnswerModel>) -> Unit
) {
    var questionText by remember { mutableStateOf(question?.question ?: "") }
    val answers = remember { mutableStateListOf<AnswerModel>() }

    LaunchedEffect(question) {
        answers.clear()
        answers.addAll(question?.answers ?: emptyList())
    }

    var answerText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (question == null) "Add New Question" else "Editar Pregunta") },
        text = {
            Column {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                            label = { Text("Answer") },
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
                            Icon(Icons.Default.Add, contentDescription = "Add Answer")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (questionText.isNotBlank() && answers.isNotEmpty() && answers.any { it.isCorrect }) {
                        onSave(questionText, answers.toList())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
