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
import com.grupo18.twister.core.viewmodel.TwistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloTwist(
    navController: NavController,
    twist: TwistModel?,
    twistViewModel: TwistViewModel
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
                    SoloTwistContent(questionsState.value)
                }
            }
        }
    }
}

@Composable
fun SoloTwistContent(questions: List<QuestionModel>) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userSelectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    val totalQuestions = questions.size
    val currentQuestion = questions[currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pregunta ${currentQuestionIndex + 1} / $totalQuestions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Opciones con feedback
        Column(
            modifier = Modifier.fillMaxWidth(),
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

        // Flechas de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                enabled = currentQuestionIndex > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
            }

            IconButton(
                onClick = { if (currentQuestionIndex < totalQuestions - 1) currentQuestionIndex++ },
                enabled = currentQuestionIndex < totalQuestions - 1
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
            }
        }
    }
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
        // Usuario seleccionó algo
        backgroundColor = when {
            isUserChoice && answer.isCorrect -> Color(0xFFB2FFB2)
            isUserChoice && !answer.isCorrect -> Color(0xFFFFB2B2)
            !isUserChoice && answer.isCorrect -> Color(0xFFE2FFD9)
            else -> MaterialTheme.colorScheme.surface
        }
        textColor = MaterialTheme.colorScheme.onSurface
    } else {
        // Aún no ha seleccionado
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        textColor = MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = {
            userSelectedAnswers[questionIndex] = answerIndex
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
