package com.grupo18.twister.core.screens.authentication

import androidx.lifecycle.ViewModel
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    fun createUser(username: String, email: String, password: String) {
        val user = UserModel(username, email, password)

        apiService.createUser(user).enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val createdUser = response.body()
                    println("Usuario creado exitosamente: $createdUser")
                } else {
                    println("Error al crear usuario: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                println("Error de red: ${t.message}")
            }
        })
    }

    fun getUser(id: String) {
        apiService.getUser(id).enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    println("Usuario obtenido: $user")
                } else {
                    println("Error al obtener usuario: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                println("Error de red: ${t.message}")
            }
        })
    }
}
