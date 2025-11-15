package com.example.ahorra.modelos

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para enviar las credenciales al endpoint /login de la API.
 * * NOTA: Los nombres de las variables deben coincidir con los campos
 * que espera el servidor de Render (ej. 'email' y 'clave' si tu servidor
 * espera esos nombres).
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("contrasena") val clave: String
)
