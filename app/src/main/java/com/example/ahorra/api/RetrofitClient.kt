package com.example.ahorra.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Cliente Singleton de Retrofit para gestionar las peticiones a la API.
 * Utiliza un interceptor para ver logs de peticiones y respuestas.
 */
object RetrofitClient {

    // URL BASE FINAL DE LA API EN RENDER
    // Importante: Debe terminar con un slash (/)
    private const val BASE_URL = "https://ahorrape-api.onrender.com/"

    val instance: ApiService by lazy {

        // 1. Crear el Interceptor de Logging para OkHttpClient
        val logging = HttpLoggingInterceptor().apply {
            // Esto permite ver la URL, headers y el cuerpo JSON en Logcat
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        // 2. Crear el OkHttpClient y añadir el Interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // 3. Crear la instancia de Retrofit
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Usamos el cliente con el logging
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}