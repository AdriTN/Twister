package com.grupo18.twister.core.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.models.TwistModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TwistViewModel : ViewModel() {

    // Estado de la lista de Twists
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists

    // Función para crear un nuevo Twist y devolverlo
    fun createTwist(title: String, description: String, imageUri: Uri?): TwistModel {
        val newTwist = TwistModel(title = title, description = description, imageUri = imageUri)
        _twists.value += newTwist
        return newTwist
    }

    // Función para obtener un Twist por su ID
    fun getTwistById(id: String): TwistModel? {
        return _twists.value.find { it.id == id }
    }

    // Función para eliminar un Twist
    fun deleteTwist(id: String) {
        _twists.value = _twists.value.filter { it.id != id }
    }
}
