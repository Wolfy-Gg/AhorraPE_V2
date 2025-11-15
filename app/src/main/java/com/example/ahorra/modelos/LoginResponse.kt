package com.example.ahorra.modelos

import com.google.gson.annotations.SerializedName

/**
 * Data class para recibir la respuesta del endpoint de inicio de sesión (/login).
 * * NOTA IMPORTANTE: Usamos "Int?", "String?" y "Boolean?" (nullable) para que Retrofit
 * pueda manejar correctamente las respuestas de error del servidor donde algunos campos
 * como idUsuario pueden ser null.
 * * Las propiedades usan camelCase (idUsuario, nombreUsuario) para asegurar que
 * los getters generados en Java sean: getIdUsuario(), getNombreUsuario().
 * * Se usa @SerializedName para mapear los nombres del servidor (snake_case) a Kotlin (camelCase).
 */
data class LoginResponse(

    @SerializedName("exito")
    val exito: Boolean?,

    @SerializedName("id_usuario")
    val idUsuario: Int?,

    @SerializedName("nombre_usuario")
    val nombreUsuario: String?,

    @SerializedName("error")
    val error: String?
)
