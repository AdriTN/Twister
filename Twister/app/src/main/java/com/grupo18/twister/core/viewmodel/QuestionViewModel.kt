package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.AnswerModel
import com.grupo18.twister.core.models.QuestionModel
import com.grupo18.twister.core.models.TwistModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class QuestionViewModel(private val apiService: ApiService) : ViewModel() {

    private val _questionsByTwist = MutableStateFlow<Map<String, List<QuestionModel>>>(emptyMap())

    fun getQuestionsForTwist(twistId: String) =
        _questionsByTwist.map { it[twistId] ?: emptyList() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun createQuestion(twistId: String, questionText: String, answers: List<AnswerModel>) {
        val newQuestion = QuestionModel(
            id = UUID.randomUUID().toString(),
            question = questionText,
            answers = answers
        )

        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions + newQuestion
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun editQuestion(twistId: String, questionId: String, questionText: String, answers: List<AnswerModel>) {
        val updatedQuestion = QuestionModel(
            id = questionId,
            question = questionText,
            answers = answers
        )

        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.map { question ->
            if (question.id == questionId) updatedQuestion else question
        }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun deleteQuestion(twistId: String, questionId: String) {
        val currentQuestions = _questionsByTwist.value[twistId] ?: emptyList()
        val updatedQuestions = currentQuestions.filter { it.id != questionId }
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            this[twistId] = updatedQuestions
        }
    }

    fun saveChanges(
        token: String,
        twistId: String,
        title: String,
        description: String,
        imageUri: String?,
        onSuccess: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val questionsToSave = _questionsByTwist.value[twistId] ?: emptyList()
                val newQuizz = TwistModel(id = twistId, title = title, description = description, imageUri = imageUri, twistQuestions = questionsToSave)

                val response = apiService.editTwist(token = token, id = twistId, twistData = newQuizz)

                response.enqueue(object : Callback<TwistModel> {
                    override fun onResponse(call: Call<TwistModel>, response: Response<TwistModel>) {
                        println("onResponse: ${response.code()}")
                        if (response.isSuccessful) {
                            val updatedTwist = response.body()
                            println("Twist actualizado exitosamente: $updatedTwist")
                            onSuccess(true)
                        } else {
                            println("Error al actualizar el twist: ${response.errorBody()?.string()}")
                            onSuccess(false)
                        }
                    }

                    override fun onFailure(call: Call<TwistModel>, t: Throwable) {
                        println("onFailure: ${t.message}")
                        onSuccess(false)
                    }
                })
            } catch (e: Exception) {
                println("Error al guardar cambios: ${e.message}")
                onSuccess(false)
            }
        }
    }

    fun hasAtLeastOneCorrectAnswer(twistId: String): Boolean {
        val questions = _questionsByTwist.value[twistId] ?: return false
        if (questions.isEmpty()) return false
        return questions.any { question -> question.answers.any { it.isCorrect } }
    }

    fun reloadDataFromApi(twistId: String) {
        _questionsByTwist.value = _questionsByTwist.value.toMutableMap().apply {
            remove(twistId)
        }
    }
}
