package com.grupo18.twister.core.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.ImageService
import com.grupo18.twister.core.models.TwistModel
import com.grupo18.twister.core.models.UploadResponse
import com.grupo18.twister.core.screens.authentication.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID

class TwistViewModel(private val myApp: MyApp) : ViewModel() {
    private val _twists = MutableStateFlow<List<TwistModel>>(emptyList())
    val twists: StateFlow<List<TwistModel>> = _twists

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    fun createTwist(title: String, description: String, imageUri: String? = null): TwistModel {
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
                e.printStackTrace()
            }
        }
    }

    fun getTwistById(id: String): TwistModel? {
        return _twists.value.find { it.id == id }
    }

    fun clearTwists() {
        _twists.value = emptyList()
    }

    fun loadTwists(
        token: String,
        scope: CoroutineScope,
        context: Context,
        onLoading: (Boolean) -> Unit
    ) {
        onLoading(true)
        scope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUserTwists(token).execute()
                if (response.isSuccessful) {
                    val twists = response.body()?.twists ?: emptyList()
                    println("Twists obtenidos: $twists")

                    twists.forEach { twist ->
                        twist.imageUri?.let { imageUri ->
                            val localFilePath = "${context.filesDir}/images/${imageUri}"
                            downloadImagesForTwist(context, imageUri, localFilePath) { imageUpdated ->
                                if (imageUpdated) {
                                    println("Imagen actualizada para el Twist ID: ${twist.id}")
                                } else {
                                    println("La imagen ya está actualizada o no es necesaria para el Twist ID: ${twist.id}")
                                }
                            }
                        }
                    }

                    _twists.value = twists
                    onLoading(false)
                } else {
                    println("Error: ${response.code()} - ${response.message()}")
                    onLoading(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onLoading(false)
            }
        }
    }


    private fun saveImageLocally(context: Context, urlId: String, contentResolver: ContentResolver, uri: String): String {
        // Obtén el InputStream de la imagen
        val inputStream = contentResolver.openInputStream(android.net.Uri.parse(uri))
        val localFilePath = "${context.filesDir}/images/$urlId.jpg" // Define la ruta donde quieres guardar la imagen
        val file = File(localFilePath)

        // Crea el directorio si no existe
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        // Escribe la imagen en el archivo local
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return localFilePath
    }

    fun uploadImage(context : Context, uri: String, contentResolver: ContentResolver, onResult: (Response<UploadResponse>) -> Unit) {
        val filePart = ImageService.prepareImageFile(uri, contentResolver)
        filePart?.let {
            val call = apiService.uploadImage(it)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val jsonString = responseBody.string()
                            val uploadResponse = Gson().fromJson(jsonString, UploadResponse::class.java)
                            saveImageLocally(context, uploadResponse.urlId, contentResolver, uri)
                            onResult(Response.success(uploadResponse))
                        } ?: run {
                            onResult(Response.error(500, ResponseBody.create(null, "Response body is null")))
                        }
                    } else {
                        onResult(Response.error(response.code(), response.errorBody() ?: ResponseBody.create(null, "Unknown error")))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(Response.error(500, ResponseBody.create(null, t.message ?: "Unknown error")))
                }
            })
        } ?: run {
            onResult(Response.error(400, ResponseBody.create(null, "File part is null")))
        }
    }


    fun downloadImagesForTwist(
        context: Context,
        imageUri: String,
        localFilePath: String,
        onImageUpdated: (Boolean) -> Unit
    ) {
        println("Se ha solicitado descargar la imagen con URI: $imageUri")
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(localFilePath)

            // Comprobar si el archivo ya existe
            if (file.exists()) {
                println("La imagen ya existe en $localFilePath")
                // Si ya existe, verifica si necesita ser actualizada
                val lastModified = file.lastModified()
                val response = apiService.checkImageUpdate(imageUri, lastModified).execute()

                if (response.code() == 304) {
                    // La imagen no ha cambiado
                    println("La imagen no ha cambiado, no es necesario descargar.")
                    onImageUpdated(false)
                    return@launch
                } else if (response.isSuccessful) {
                    println("La imagen ha cambiado, descargando nueva versión.")
                } else {
                    println("Error al comprobar la actualización de la imagen: ${response.code()} - ${response.message()}")
                    onImageUpdated(false)
                    return@launch
                }
            } else {
                println("El archivo no existe, proceder a descargar.")
            }

            try {
                // Descargar la imagen si no existe o ha cambiado
                val downloadResponse = apiService.downloadImage(imageUri).execute()
                if (downloadResponse.isSuccessful) {
                    val body = downloadResponse.body()
                    if (body != null) {
                        val imageBytes = body.bytes()
                        if (imageBytes.isNotEmpty()) {
                            file.writeBytes(imageBytes)
                            println("Imagen descargada y guardada en $localFilePath")
                            onImageUpdated(true)
                        } else {
                            println("Error: La respuesta del servidor no contiene datos de imagen.")
                            onImageUpdated(false)
                        }
                    } else {
                        println("Error: La respuesta del servidor es nula.")
                        onImageUpdated(false)
                    }
                } else {
                    println("Error al descargar la imagen: ${downloadResponse.code()} - ${downloadResponse.message()}")
                    Toast.makeText(context, "Error al descargar la imagen", Toast.LENGTH_SHORT).show()
                    onImageUpdated(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Excepción al descargar la imagen: ${e.message}")
                onImageUpdated(false)
            }
        }
    }


}
