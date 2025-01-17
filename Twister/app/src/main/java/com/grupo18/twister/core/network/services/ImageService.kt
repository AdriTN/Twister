package com.grupo18.twister.core.network.services

import com.grupo18.twister.models.game.TwistModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ImageService {
    @Multipart
    @POST("/images/upload")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("/images/download/{imageUri}")
    fun downloadImage(
        @Header("Authorization") token: String,
        @Path("imageUri") imageUri: String
    ): Call<ResponseBody>

    @HEAD("/images/check/{imageUri}")
    fun checkImageUpdate(
        @Path("imageUri") imageUri: String,
        @Header("Authorization") token: String,
        @Header("If-Modified-Since") lastModified: Long
    ): Call<Void>

    @POST("/images/delete")
    fun deleteImage(
        @Header("Authorization") token: String,
        @Body twistData: TwistModel
    ): Call<ResponseBody>
}
