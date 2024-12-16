package com.grupo18.twister.core.screens.twists

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
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.authentication.MyApp
import com.grupo18.twister.core.screens.navigation.Routes
import com.grupo18.twister.core.viewmodel.TwistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloTwist(
    navController: NavController,
    twist: TwistModel?
) {
    val context = LocalContext.current
    val myApp = context.applicationContext as MyApp
    val token = myApp.currentUser.value?.token ?: ""

    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val questionsState = remember { mutableStateOf<List<QuestionModel>>(emptyList()) }

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
                title = { Text("Jugar en solitario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
}

@Composable
fun SoloTwistContent(questions: List<QuestionModel>, navController: NavController) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userSelectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    val totalQuestions = questions.size
    val currentQuestion = questions[currentQuestionIndex]

    var showFinishDialog by remember { mutableStateOf(false) }

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
                    userSelectedAnswers = userSelectedAnswers
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
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    Text("Finish")
                }
            }
        }

        // Diálogo de confirmación para terminar el cuestionario
        if (showFinishDialog) {
            FinishDialog(
                onConfirm = {
                    // Lógica para ir al Home (o cualquier otra pantalla que quieras)
                    navController.navigate(Routes.HOME) // Esto lleva al usuario a la pantalla anterior (Home)
                },
                onDismiss = {
                    showFinishDialog = false
                }
            )
        }
    }
}

@Composable
fun FinishDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure you want to finish?") },
        text = { Text("We recommend reviewing your answers before finishing the twist.") },
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
fun AnswerOption(
    questionIndex: Int,
    answerIndex: Int,
    answer: AnswerModel,
    userSelectedAnswers: MutableMap<Int, Int>
) {
    val userChoice = userSelectedAnswers[questionIndex]
    val isUserChoice = (userChoice == answerIndex)
    val backgroundColor: Color
    val textColor: Color

    if (userChoice != null) {
        // Colorear según la selección del usuario
        backgroundColor = when {
            isUserChoice && answer.isCorrect -> Color(0xFFB2FFB2) // Verde si es correcto
            isUserChoice && !answer.isCorrect -> Color(0xFFFFB2B2) // Rojo si es incorrecto
            !isUserChoice && answer.isCorrect -> Color(0xFFE2FFD9) // Verde claro para correctas no seleccionadas
            else -> MaterialTheme.colorScheme.surface
        }
        textColor = MaterialTheme.colorScheme.onSurface
    } else {
        // Estado inicial sin seleccionar
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        textColor = MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = {
            if (userChoice == null) { // Solo se permite seleccionar si no se ha hecho una selección
                userSelectedAnswers[questionIndex] = answerIndex
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        enabled = userChoice == null // Solo habilitado si no se ha seleccionado una respuesta
    ) {
        Text(answer.text, color = textColor)
    }
}
