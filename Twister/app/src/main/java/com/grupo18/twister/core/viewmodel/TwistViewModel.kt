package com.grupo18.twister.core.viewmodel

import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.models.TwistModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TwistViewModel : ViewModel() {

    // Estado de la lista de Twists
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists

    // Función para crear un nuevo Twist
    fun createTwist(title: String, description: String) {
        val newTwist = TwistModel(title = title, description = description)
        _twists.value += newTwist
    }

    // Función para editar un Twist existente
    fun editTwist(id: String, newTitle: String, newDescription: String) {
        _twists.value = _twists.value.map { twist ->
            if (twist.id == id) {
                twist.copy(
                    title = newTitle,
                    description = newDescription,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                twist
            }
        }
    }

    // Función para eliminar un Twist
    fun deleteTwist(id: String) {
        _twists.value = _twists.value.filter { it.id != id }
    }
}
