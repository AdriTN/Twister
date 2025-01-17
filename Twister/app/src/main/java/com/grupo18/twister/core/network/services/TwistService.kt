package com.grupo18.twister.core.network.services

import com.grupo18.twister.models.game.TwistModel
import com.grupo18.twister.models.network.domain.TwistRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TwistService {

    @GET("/twists/get")
    fun getUserTwists(
        @Header("Authorization") token: String
    ): Call<TwistRequest>

    @PUT("/twists/edit")
    fun editTwist(
        @Header("Authorization") token: String,
        @Body twistData: TwistModel
    ): Call<TwistModel>

    @DELETE("/twists/delete/{id}")
    fun deleteTwist(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<ResponseBody>

    @GET("/twists/public")
    fun getPublicTwists(
        @Header("Authorization") token: String
    ): Call<List<TwistModel>>

    @GET("/twists/public/{twistId}")
    fun getPublicTwistById(
        @Path("twistId") twistId: String
    ): Call<TwistModel>
}
