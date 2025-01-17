package com.grupo18.twister.core.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

object ImageProcessor {

    /**
     * Extrae el color dominante de una imagen localizada en [context.filesDir]/images/[uri]
     * Retorna [Color.TRANSPARENT] si el archivo no existe o no puede extraer el color.
     */
    @SuppressLint("NewApi")
    fun extractDominantColorFromUri(uri: String, context: Context): Int {
        val destinationPath = "${context.filesDir}/images/$uri"
        val file = File(destinationPath)
        if (!file.exists()) {
            return Color.TRANSPARENT
        }

        val inputStream = context.contentResolver.openInputStream(Uri.fromFile(file))
            ?: throw FileNotFoundException("Unable to open InputStream for URI: $destinationPath")

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
            ?: return Color.TRANSPARENT

        // Si el Bitmap está en HARDWARE, copiamos a ARGB_8888
        val bitmap = if (originalBitmap.config == Bitmap.Config.HARDWARE) {
            originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            originalBitmap
        }

        val dominantColor = Palette.from(bitmap).generate().getDominantColor(Color.TRANSPARENT)

        originalBitmap.recycle()
        bitmap.recycle()

        return dominantColor
    }
}