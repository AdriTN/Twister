package com.grupo18.twister.core.services

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object ImageUploadUtils {

    fun prepareImageFile(fileName: String): MultipartBody.Part? {
        val file = File(fileName)
        if (!file.exists()) {
            Log.e("ImageUploadUtils", "File not found: ${file.absolutePath}")
            return null
        }

        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("image", file.name, requestBody)
    }
}
