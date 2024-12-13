// Archivo: QuestionViewModelFactory.kt
package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo18.twister.core.models.TwistModel

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
