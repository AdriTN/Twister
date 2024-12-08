package com.grupo18.twister.core.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.ImageService
import com.grupo18.twister.core.models.TokenVerificationResponse
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UploadResponse
import com.grupo18.twister.core.screens.authentication.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class TwistViewModel(private val myApp: MyApp) : ViewModel() {
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists
    private val _uploadState = MutableLiveData<Result<UploadResponse>>()
    val uploadState: LiveData<Result<UploadResponse>> = _uploadState

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    fun createTwist(title: String, description: String, imageUri: Uri? = null): TwistModel {
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

    fun deleteTwist(id: String) {
        _twists.value = _twists.value.filter { it.id != id }
    }

    fun getTwistById(id: String): TwistModel? {
        return _twists.value.find { it.id == id }
    }

    suspend fun loadTwists(
        token: String,
        scope: CoroutineScope,
        onLoadingFinished: (Boolean) -> Unit
    ): List<TwistModel>? {
        return try {
            // Realiza la llamada a la API en el hilo IO
            val response = withContext(Dispatchers.IO) {
                apiService.getUserTwists(token).execute()
            }

            println("Respuesta del servidor: ${response.body()}")

            // Comprobamos si la respuesta fue exitosa
            if (response.isSuccessful) {
                // Llamar a onLoadingFinished con true si los twists se cargaron correctamente
                onLoadingFinished(true)
                return response.body() as List<TwistModel>? // Retornamos el cuerpo de la respuesta como una lista de Twists
            } else {
                // Llamar a onLoadingFinished con false en caso de que haya un error de respuesta
                onLoadingFinished(false)
                // Mostrar el error recibido desde el servidor si es necesario
                println("Error: ${response.code()} - ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            // En caso de error, se llama a onLoadingFinished con false
            onLoadingFinished(false)
            // Manejo del error, por ejemplo, se puede loguear la excepción
            e.printStackTrace()
            return null
        }
    }



    fun uploadImage(uri: Uri, contentResolver: ContentResolver, onResult: (Response<ResponseBody>) -> Unit) {
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
