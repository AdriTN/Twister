package com.grupo18.twister.core.api

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.palette.graphics.Palette
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

class ImageService(private val apiService: ApiService) {
    @SuppressLint("NewApi")
    fun extractDominantColorFromUri(uri: String, context: Context): Int {
        println("Extracting dominant color from URI: $uri")
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

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
        fun prepareImageFile(uri: String, contentResolver: ContentResolver): MultipartBody.Part? {
            val inputStream = contentResolver.openInputStream(Uri.parse(uri)) ?: return null
            val tempFile = File.createTempFile("image", ".jpg")
            tempFile.outputStream().use { inputStream.copyTo(it) }

            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            return MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
        }

        fun uploadImage(quizzId: String, uri: Uri, contentResolver: ContentResolver, apiService: ApiService): Call<ResponseBody> {
            // Obtén el nombre del archivo
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "image.jpg"
            val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Invalid URI")
            val filePart = MultipartBody.Part.createFormData(
                "image",
                fileName,
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), inputStream.readBytes())
            )

            return apiService.uploadImage(filePart)
        }
    }
}
