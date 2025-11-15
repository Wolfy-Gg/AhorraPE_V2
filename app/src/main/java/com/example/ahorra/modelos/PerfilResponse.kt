package com.example.ahorra.modelos

import com.google.gson.annotations.SerializedName

// Estructura de datos para la respuesta del perfil del usuario (una vez autenticado)
data class PerfilResponse(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("saldo_total") val saldoTotal: Double,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    @SerializedName("exito") val exito: Boolean? = null,
    @SerializedName("error") val error: String? = null
)
