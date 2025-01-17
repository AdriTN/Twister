package com.grupo18.twister.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grupo18.twister.models.game.AnswerModel
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.main.MyApp
import com.grupo18.twister.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloTwist(
    navController: NavController,
    twist: TwistModel?
) {
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val questionsState = remember { mutableStateOf<List<QuestionModel>>(emptyList()) }

    val showExitDialog = remember { mutableStateOf(false) }

    LaunchedEffect(twist) {
        isLoading.value = true
        errorMessage.value = null
        try {
            val questions = twist?.twistQuestions
            if (questions != null) {
                questionsState.value = questions
            }
        } catch (e: Exception) {
            errorMessage.value = e.localizedMessage
        } finally {
            isLoading.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (twist != null) {
                        Text("Playing Solo: ${twist.title}")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog.value = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading.value -> {
                    CircularProgressIndicator()
                }
                !errorMessage.value.isNullOrEmpty() -> {
                    Text(text = "Error: ${errorMessage.value}", color = Color.Red)
                }
                questionsState.value.isEmpty() -> {
                    Text(text = "No hay preguntas para este Twist.")
                }
                else -> {
                    // Muestra la UI de preguntas con feedback
                    SoloTwistContent(questionsState.value, navController)
                }
            }
        }
    }

    // Diálogo para confirmar si el usuario desea salir
    if (showExitDialog.value) {
        ExitDialog(
            onConfirm = {
                navController.popBackStack()
            },
            onDismiss = {
                showExitDialog.value = false
            }
        )
    }
}

@Composable
fun ExitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure you want to exit the quiz?") },
        text = { Text("Answers will not be saved.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@Composable
fun SoloTwistContent(questions: List<QuestionModel>, navController: NavController) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val totalQuestions = questions.size

    // userAnswers: lista donde cada elemento es un estado con el set de índices seleccionados para esa pregunta.
    val userAnswers = remember(questions) {
        List(questions.size) { mutableStateOf(setOf<Int>()) }
    }

    var showFinishDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0f) }  // Lo usaremos como Float para permitir parciales

    val currentQuestion = questions[currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de progreso con progreso numérico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1) / totalQuestions.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "${currentQuestionIndex + 1} / $totalQuestions",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 6.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Pregunta actual
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Opciones de respuesta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentQuestion.answers.forEachIndexed { answerIndex, answer ->
                AnswerOption(
                    questionIndex = currentQuestionIndex,
                    answerIndex = answerIndex,
                    answer = answer,
                    userAnswers = userAnswers
                )
            }
        }

        // Flechas de navegación o el botón de "Finish" en la última pregunta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Flecha de "Anterior"
            IconButton(
                onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                enabled = currentQuestionIndex > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
            }

            // Si es la última pregunta, mostrar botón "Finish", si no, flecha de siguiente
            if (currentQuestionIndex < totalQuestions - 1) {
                IconButton(
                    onClick = { if (currentQuestionIndex < totalQuestions - 1) currentQuestionIndex++ },
                    enabled = currentQuestionIndex < totalQuestions - 1
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
            } else {
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Finish")
                }
            }
        }

        // Diálogo de confirmación para terminar el cuestionario
        if (showFinishDialog) {
            FinishDialog(
                onConfirm = {
                    // Calcular la puntuación con parcial
                    score = calculatePartialScore(userAnswers, questions)
                    showFinishDialog = false
                    showScoreDialog = true
                },
                onDismiss = {
                    showFinishDialog = false
                }
            )
        }

        // Diálogo con la puntuación final (ahora Float con parciales)
        if (showScoreDialog) {
            ScoreDialog(
                score = score,
                totalQuestions = totalQuestions.toFloat(),  // Convertimos a Float para mostrar con decimales
                onConfirm = {
                    navController.navigate(Routes.HOME) // Volver al Home
                },
                onDismiss = {
                    showScoreDialog = false
                }
            )
        }
    }
}

@Composable
fun AnswerOption(
    questionIndex: Int,
    answerIndex: Int,
    answer: AnswerModel,
    userAnswers: List<MutableState<Set<Int>>>
) {
    val selectedAnswers = userAnswers[questionIndex].value
    val isSelected = answerIndex in selectedAnswers

    val backgroundColor = if (isSelected) Color(0xFFADD8E6) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Button(
        onClick = {
            val newSet = selectedAnswers.toMutableSet()
            if (isSelected) {
                newSet.remove(answerIndex)
            } else {
                newSet.add(answerIndex)
            }
            userAnswers[questionIndex].value = newSet
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(answer.text, color = textColor)
    }
}

/**
 * Calcula la puntuación parcial. Si una pregunta tiene múltiples respuestas correctas e
 * incorrectas, se asigna una puntuación entre 0 y 1 según cuántas correctas se eligieron
 * y cuántas incorrectas. Finalmente, se suman las puntuaciones de todas las preguntas.
 */
fun calculatePartialScore(
    userAnswers: List<MutableState<Set<Int>>>,
    questions: List<QuestionModel>
): Float {
    var totalScore = 0f

    questions.forEachIndexed { qIndex, question ->
        val selectedSet = userAnswers[qIndex].value

        val correctIndices = question.answers.mapIndexedNotNull { aIndex, ans ->
            if (ans.isCorrect) aIndex else null
        }
        val incorrectIndices = question.answers.mapIndexedNotNull { aIndex, ans ->
            if (!ans.isCorrect) aIndex else null
        }

        val totalCorrect = correctIndices.size
        val totalIncorrect = incorrectIndices.size

        // Cuántas correctas seleccionó el usuario
        val correctSelected = correctIndices.count { it in selectedSet }
        // Cuántas incorrectas seleccionó el usuario
        val incorrectSelected = incorrectIndices.count { it in selectedSet }

        if (totalCorrect == 0) {
            // Si no hay correctas (caso raro), le damos 0. O manejarlo según tu lógica.
            return@forEachIndexed
        }

        // Fracción de correctas e incorrectas seleccionadas
        val fractionCorrect = correctSelected.toFloat() / totalCorrect.toFloat()
        val fractionIncorrect = if (totalIncorrect > 0) {
            incorrectSelected.toFloat() / totalIncorrect.toFloat()
        } else 0f

        // Puntuación parcial de la pregunta
        var partial = fractionCorrect - fractionIncorrect
        if (partial < 0f) partial = 0f
        if (partial > 1f) partial = 1f

        totalScore += partial
    }

    // totalScore es la suma de los parciales de cada pregunta.
    // Esto puede dar un valor entre 0 y el número de preguntas.
    return totalScore
}

@Composable
fun ScoreDialog(
    score: Float,
    totalQuestions: Float,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quiz Completed") },
        text = {
            // Mostramos la puntuación parcial, ej: "3.5 / 5.0"
            Text("Your score: %.2f / %.0f".format(score, totalQuestions))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Go to Home")
            }
        }
    )
}

@Composable
fun FinishDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure you want to finish?") },
        text = { Text("Make sure you've selected all the answers you want.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

