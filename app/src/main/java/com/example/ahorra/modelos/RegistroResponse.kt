package com.example.ahorra.modelos

import com.google.gson.annotations.SerializedName

// Este modelo mapea tanto la respuesta de ÉXITO (201) como los errores (409)
data class RegistroResponse(
    // 201: {"status": 201, "mensaje": "Usuario registrado exitosamente.", "usuario_id": 5}
    // 409: {"status": 409, "mensaje": "Error de duplicidad...", "error": "detalle de error"}
    @SerializedName("status") val status: Int,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("usuario_id") val idUsuario: Int? = null, // Nullable si es un error 409
    @SerializedName("error") val error: String? = null // Presente solo en caso de errores (409)
)
