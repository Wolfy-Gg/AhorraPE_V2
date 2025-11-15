package com.example.ahorra.modelos

import com.google.gson.annotations.SerializedName

// Este modelo define la estructura JSON que se envía al endpoint POST /registro
data class RegistroRequest(
    @SerializedName("dni") val dni: String?, // Opcional
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String?, // Formato YYYY-MM-DD
    @SerializedName("sexo") val sexo: String?, // 'M', 'F', 'N'
    @SerializedName("email") val email: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("id_distrito") val idDistrito: Int?, // Opcional
    @SerializedName("id_moneda") val idMoneda: Int // Obligatorio
)