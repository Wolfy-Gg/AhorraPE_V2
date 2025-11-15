package com.example.ahorra.api

import com.example.ahorra.modelos.RegistroRequest
import com.example.ahorra.modelos.RegistroResponse
// Importamos los nuevos modelos de Login
import com.example.ahorra.modelos.LoginRequest
import com.example.ahorra.modelos.LoginResponse
import com.example.ahorra.modelos.PerfilResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @POST("registro")
    fun registrarUsuario(@Body request: RegistroRequest): Call<RegistroResponse>

    @POST("login")
    fun iniciarSesion(@Body request: LoginRequest): Call<LoginResponse>

    @GET("perfil")
    fun obtenerPerfil(
        @Header("Authorization") authorization: String
    ): Call<PerfilResponse>

}

