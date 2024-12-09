package com.grupo18.twister.core.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.ImageService
import com.grupo18.twister.core.models.ImageUri
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.screens.authentication.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class TwistViewModel(private val myApp: MyApp) : ViewModel() {
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    fun createTwist(title: String, description: String, imageUri: ImageUri? = null): TwistModel {
        val newId = UUID.randomUUID().toString()
        val newTwist = TwistModel(id = newId, title = title, description = description, imageUri = imageUri)
        _twists.value += newTwist
        return newTwist
    }

    fun updateTwist(updatedTwist: TwistModel) {
        _twists.value = _twists.value.map { twist ->
            if (twist.id == updatedTwist.id) updatedTwist else twist
        }
    }

    fun deleteTwist(token: String, id: String, scope: CoroutineScope, context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val response = apiService.deleteTwist(token, id).execute()
                if (response.isSuccessful) {
                    _twists.value = _twists.value.filter { it.id != id }
                } else {
                    println("Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Error al eliminar el Twist", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception) {
                // Maneja la excepción y llama a onLoadingFinished con false
                e.printStackTrace()
            }
        }
    }

    fun getTwistById(id: String): TwistModel? {
        return _twists.value.find { it.id == id }
    }

    fun getTwists(): List<TwistModel> {
        return _twists.value
    }

    fun clearTwists() {
        _twists.value = emptyList()
    }

    fun loadTwists(
        token: String,
        scope: CoroutineScope,
        onLoading: (Boolean) -> Unit
    ) {
        scope.launch(Dispatchers.IO) { // Utiliza el scope proporcionado
            try {
                // Realiza la llamada a la API
                val response = apiService.getUserTwists(token).execute()
                response.body()?.twists?.forEach { twist ->  // Cambia collect por forEach
                    val imageUri = twist.imageUri?.uri ?: "No hay imagen disponible"
                    println("ID: ${twist.id}, Title: ${twist.title}, Image URI: $imageUri")
                }

                // Verifica si la respuesta fue exitosa
                if (response.isSuccessful) {
                    val twists = response.body()?.twists ?: emptyList()
                    println("Twists obtenidos: $twists")
                    _twists.value = twists // Actualiza el estado con los twists obtenidos
                    onLoading(false) // Indica que la carga fue exitosa
                } else {
                    // Maneja el error y llama a onLoadingFinished con false
                    println("Error: ${response.code()} - ${response.message()}")
                    onLoading(false)
                }
            } catch (e: Exception) {
                // Maneja la excepción y llama a onLoadingFinished con false
                e.printStackTrace()
                onLoading(false)
            }
        }
    }

    fun uploadImage(uri: String, contentResolver: ContentResolver, onResult: (Response<ResponseBody>) -> Unit) {
        val filePart = ImageService.prepareImageFile(uri, contentResolver)
        filePart?.let {
            val call = apiService.uploadImage(it)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    onResult(response)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(Response.error(500, ResponseBody.create(null, t.message ?: "Unknown error")))
                }
            })
        } ?: run {
            onResult(Response.error(400, ResponseBody.create(null, "File part is null")))
        }
    }

}
