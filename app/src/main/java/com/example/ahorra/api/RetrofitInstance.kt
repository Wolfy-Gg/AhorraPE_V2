package com.example.ahorra.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // ⚠️ IMPORTANTE: Reemplaza ESTA URL con la URL base de tu hosting (por ejemplo, "http://tu-dominio.great-site.net/").
    // La URL debe terminar en un slash (/).
    private const val BASE_URL = "https://ahorrape-api.onrender.com/"

    // 1. Configuración de Gson con setLenient(true)
    // Esto le dice al parser que ignore caracteres extraños (como los inyectados por el firewall)
    // que puedan aparecer antes del JSON real.
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // 2. Cliente HTTP (OkHttpClient) con el User-Agent y timeouts
    private val client = OkHttpClient.Builder()
        // Aseguramos que el User-Agent se envía para ayudar a engañar al firewall
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .method(original.method, original.body)
            chain.proceed(requestBuilder.build())
        }
        // Añadir timeouts por si acaso la conexión es lenta
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 3. Creación de la instancia de Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Usamos el cliente HTTP personalizado
            .client(client)
            // Usamos el GsonConverterFactory configurado como Lenient
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Método para obtener la API Service
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}