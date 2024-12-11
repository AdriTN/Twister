package com.grupo18.twister.core.api

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.palette.graphics.Palette
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.FileNotFoundException

class ImageService(private val apiService: ApiService) {
    @SuppressLint("NewApi")
    fun extractDominantColorFromUri(uri: String, context: Context): Int {
        println("Extracting dominant color from URI: $uri")
        val destinationPath = "${context.filesDir}/images/${uri}"
        // Comprobar si el archivo existe localmente
        val file = File(destinationPath)
        if (!file.exists()) {
            println("File does not exist: $destinationPath")
            return Color.TRANSPARENT // Retornar un color predeterminado o manejar el error
        }

        // Abrir InputStream usando ContentResolver
        val inputStream = context.contentResolver.openInputStream(Uri.fromFile(file))
            ?: throw FileNotFoundException("Unable to open InputStream for URI: $destinationPath")

        // Crear Bitmap desde el InputStream
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Unable to decode Bitmap from URI: $destinationPath")

        // Verificar si el bitmap está en formato hardware
        val bitmap = if (originalBitmap.config == Bitmap.Config.HARDWARE) {
            // Convertir a ARGB_8888
            originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            originalBitmap
        }

        // Usar Palette para obtener el color dominante
        val palette = Palette.from(bitmap).generate()
        val dominantColor = palette.getDominantColor(Color.TRANSPARENT)

        // Liberar recursos
        originalBitmap.recycle()
        bitmap.recycle()

        return dominantColor
    }

    companion object {
        fun prepareImageFile(fileName: String): MultipartBody.Part? {
            val file = File(fileName) // Suponiendo que el archivo está en cacheDir
            if (!file.exists()) {
                Log.e("ImageService", "File not found: ${file.absolutePath}")
                return null
            }

            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            return MultipartBody.Part.createFormData("image", file.name, requestBody)
        }
    }
}
