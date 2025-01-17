package com.grupo18.twister.ui.screens.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo18.twister.models.game.QuestionModel
import com.grupo18.twister.models.game.AnswerModel


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
        title = { Text(text = if (question == null) "Add New Question" else "Edit Question") },
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
