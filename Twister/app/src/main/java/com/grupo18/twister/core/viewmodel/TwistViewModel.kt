package com.grupo18.twister.core.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.authentication.MyApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TwistViewModel(private val myApp: MyApp) : ViewModel() {

    // Estado de la lista de Twists
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists
    // Estado de la lista de Questions
    private val _questions = MutableStateFlow<List<QuestionModel>>(emptyList())
    val questions: StateFlow<List<QuestionModel>> = _questions
    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)


    // Función para cargar Twists desde la API
    private fun loadTwists() {
        viewModelScope.launch {
            try {
                val currentUser = myApp.getUser().value // Obtener el usuario actual
                if (currentUser != null) {
                    val token = currentUser.token // Acceder al token
                    val fetchedTwists = apiService.getUserTwists(token).execute().body()
                    _twists.value = fetchedTwists as List<TwistModel>
                } else {
                    // Manejar el caso donde el usuario no esté logueado
                    _twists.value = emptyList()
                }
            } catch (e: Exception) {
                // Manejo de errores
                e.printStackTrace()
            }
        }
    }

    // Función para crear un nuevo Twist
    fun createTwist(title: String, description: String) {
        val newTwist = TwistModel(title = title, description = description)
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
