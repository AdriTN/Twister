package com.grupo18.twister.core.screens.twists

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo18.twister.core.components.ContentCard
import com.grupo18.twister.core.components.OptionsList
import com.grupo18.twister.core.components.ProgressBar

@Composable
fun SoloTwist(quizData: Question) {
    val (currentQuestionIndex, setCurrentQuestionIndex) = remember { mutableStateOf(0) }
    val userAnswers = remember { mutableStateOf(mutableMapOf<Int, String>()) }  // Mapa para almacenar respuestas por índice

    SoloTwistContent(
        quizData = quizData,
        currentQuestionIndex = currentQuestionIndex,
        onNavigateToQuestion = setCurrentQuestionIndex,
        userAnswers = userAnswers
    )
}

@Composable
fun SoloTwistContent(
    quizData: Question,
    currentQuestionIndex: Int,
    onNavigateToQuestion: (Int) -> Unit,
    userAnswers: MutableState<MutableMap<Int, String>>  // Ahora pasamos userAnswers como un estado mutable
) {
    val currentQuestion = quizData.questions[currentQuestionIndex]
    val totalQuestions = quizData.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de progreso
        ProgressBar(progress = (currentQuestionIndex + 1) / totalQuestions.toFloat())

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de contenido mostrando la descripción y la imagen de la pregunta actual
        ContentCard(
            description = currentQuestion.description,
            image = currentQuestion.image
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de opciones para la pregunta actual
        OptionsList(
            options = currentQuestion.options,
            selected = userAnswers.value[currentQuestionIndex],
            onOptionSelected = { selectedOption ->
                userAnswers.value[currentQuestionIndex] = selectedOption
                println("Opción seleccionada: $selectedOption")
                println("Mapa actualizado: ${userAnswers.value}")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de navegación para avanzar y retroceder entre preguntas
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Botón "Anterior"
            Button(
                onClick = { onNavigateToQuestion(currentQuestionIndex - 1) },
                enabled = currentQuestionIndex > 0 // Deshabilitar si es la primera pregunta
            ) {
                Text("Anterior")
            }

            // Botón "Siguiente"
            Button(
                onClick = { onNavigateToQuestion(currentQuestionIndex + 1) },
                enabled = currentQuestionIndex < totalQuestions - 1 // Deshabilitar si es la última pregunta
            ) {
                Text("Siguiente")
            }
        }
    }
}
