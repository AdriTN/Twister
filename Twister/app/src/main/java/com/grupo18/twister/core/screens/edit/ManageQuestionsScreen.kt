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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.navigation.Routes
import com.grupo18.twister.core.viewmodel.QuestionViewModel
import com.grupo18.twister.core.viewmodel.QuestionViewModelFactory
import com.grupo18.twister.core.viewmodel.TwistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageQuestionsScreen(
    navController: NavController,
    token: String,
    twistViewModel: TwistViewModel,
    scope: CoroutineScope,
    twist: TwistModel
) {
    val questionViewModel: QuestionViewModel = viewModel(
        factory = QuestionViewModelFactory(twist)
    )
    val questions by questionViewModel.questions.collectAsState()

    println("Questions: $questions of questionViewModel $questionViewModel")

    var showQuestionDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<QuestionModel?>(null) }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Questions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (questions.size < 25) {
                        IconButton(onClick = {
                            selectedQuestion = null
                            showQuestionDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Question")
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                if (questions.isEmpty()) {
                    Text("No questions available. Add at least one with a correct answer before saving.", color = Color.Gray)
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
                                    questionViewModel.deleteQuestion(scope, question.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (showError) {
                    Text("You must have at least one question with a correct answer to save.", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val valid = questionViewModel.hasAtLeastOneCorrectAnswer()
                        if (!valid) {
                            showError = true
                        } else {
                            // Guardar los cambios en el backend y esperar la respuesta antes de volver
                            questionViewModel.saveChanges(scope, token) { success ->
                                if (success) {
                                    twistViewModel.loadTwists(token, scope, context = context) { loading ->
                                        if (!loading) {
                                            scope.launch(Dispatchers.Main) {
                                                navController.navigate(Routes.EDIT) {
                                                    popUpTo(Routes.EDIT) { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    println("Error saving changes to the questions.")
                                }
                            }
                        }
                    }) {
                        Text("Save")
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
                            questionViewModel.createQuestion(scope, questionText, answers)
                        } else {
                            questionViewModel.editQuestion(scope, selectedQuestion!!.id, questionText, answers)
                        }
                        showQuestionDialog = false
                        selectedQuestion = null
                    },
                    twistId = twist.id,
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
                    Icon(Icons.Default.Edit, contentDescription = "Edit Question", tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminate Question", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
