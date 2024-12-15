package com.grupo18.twister.core.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.api.ImageService
import com.grupo18.twister.core.models.QuestionModel
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
import java.io.FileNotFoundException
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

    fun updateTwist(token: String, twist: TwistModel, scope: CoroutineScope, context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val response = apiService.editTwist(token, twist).execute()
                if (response.isSuccessful) {
                    _twists.value = _twists.value.filter { it.id != twist.id }
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

    fun deleteTwist(token: String, id: String, scope: CoroutineScope, context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                if (id.isEmpty()) throw Exception("ID is empty")
                println("Se va a eliminar el Twist con ID: $id")
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

    fun getAllTwists(): List<TwistModel> {
        return _twists.value
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
                            downloadImagesForTwist(context, token, imageUri, localFilePath) { imageUpdated ->
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


    fun uploadImage(context : Context, token: String, imageUri: String, contentResolver: ContentResolver, onResult: (Response<UploadResponse>) -> Unit) {
        val filePart = ImageService.prepareImageFile(imageUri)
        filePart?.let {
            val call = apiService.uploadImage(token, it)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            val jsonString = responseBody.string()
                            val uploadResponse = Gson().fromJson(jsonString, UploadResponse::class.java)
                            val destinationPath = "${context.filesDir}/images/${uploadResponse.urlId}"
                            println("Imagen del servidor guardada en: $destinationPath")
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

    fun deleteImageFromTwist(token: String, Twist: TwistModel, scope: CoroutineScope, context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                if (Twist.imageUri?.isEmpty() == true) throw Exception("Image URI is empty")
                val response = apiService.deleteImage(token = token, twistData = Twist).execute()
                if (response.isSuccessful) {
                    println("Imagen eliminada correctamente del servidor: ${Twist.imageUri}")

                    // Eliminar la imagen localmente
                    val localFilePath = "${context.filesDir}/images/${Twist.imageUri}"
                    val file = File(localFilePath)
                    if (file.exists()) {
                        val deleted = file.delete()
                        if (deleted) {
                            println("Imagen eliminada localmente: $localFilePath")
                        } else {
                            println("No se pudo eliminar la imagen localmente: $localFilePath")
                        }
                    }

                    Toast.makeText(context, "Imagen eliminada exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    println("Error al eliminar la imagen del servidor: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Excepción al eliminar la imagen: ${e.message}")
            }
        }
    }

    fun isSameImage(currentImageUri: String?, newImageUri: String?, context: Context): Boolean {
        if (currentImageUri == null || newImageUri == null) {
            return false
        }

        val currentFile = File(currentImageUri)
        val newFile = File(newImageUri)

        println("Se va a comparar la imagen $currentFile con $newFile")

        if (!currentFile.exists() || !newFile.exists()) {
            println("No se ha encontró alguno de los archivos " + currentFile.exists() + " " + newFile.exists())
            return false
        }

        // Compara los contenidos de ambos archivos
        return currentFile.readBytes().contentEquals(newFile.readBytes())
    }





    private fun saveImageLocally(context: Context, localFilePath: String, sourceFilePath: String): String {
        // Archivo fuente (imagen local)
        val sourceFile = File(sourceFilePath)

        // Directorio destino (misma carpeta de imágenes)
        val destinationFile = File(localFilePath)

        // Verifica si el archivo fuente existe antes de intentar copiarlo
        if (!sourceFile.exists()) {
            throw FileNotFoundException("El archivo fuente no existe: $sourceFilePath")
        }

        // Copia el contenido del archivo fuente al archivo destino
        sourceFile.inputStream().use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("Archivo guardado en: ${destinationFile.absolutePath}")
        return destinationFile.absolutePath // Retorna la ruta del archivo guardado
    }


    fun downloadImagesForTwist(
        context: Context,
        token: String,
        imageUri: String,
        localFilePath: String,
        onImageUpdated: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            println("Comenzando descarga de imágenes para $imageUri en $localFilePath")
            val file = File(localFilePath)

            file.parentFile?.let { parent ->
                if (!parent.exists()) {
                    println("Creando directorio: ${parent.absolutePath}")
                    parent.mkdirs()
                }
            }

            // Comprobar si el archivo ya existe
            //if (file.exists()) {
            if (false) {
                println("La imagen ya existe en ${file.absolutePath}")
                // Verificar si necesita actualización
                val lastModified = file.lastModified()
                println("Se va a comprobar el estado de la imagen $imageUri en $lastModified")
                val response = apiService.checkImageUpdate(token, imageUri, lastModified).execute()

                println("Respuesta del servidor: ${response.code()} - ${response.message()}")

                if (response.code() == 304) {
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
                val downloadResponse = apiService.downloadImage(token, imageUri).execute()
                if (downloadResponse.isSuccessful) {
                    val body = downloadResponse.body()
                    if (body != null) {
                        val imageBytes = body.bytes()
                        if (imageBytes.isNotEmpty()) {
                            file.writeBytes(imageBytes)
                            println("Imagen descargada y guardada en ${file.absolutePath}")
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

    suspend fun loadQuestionsForTwist(twistId: String, token: String): List<QuestionModel> {
        val response = apiService.getAllQuestions("Bearer $token", twistId).execute()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error: ${response.code()} - ${response.message()}")
        }
    }
}
