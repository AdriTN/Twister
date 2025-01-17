// Archivo: QuestionViewModelFactory.kt
package com.grupo18.twister.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.viewmodels.screens.QuestionViewModel

class QuestionViewModelFactory(
    private val twist: TwistModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            return QuestionViewModel(twist) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
