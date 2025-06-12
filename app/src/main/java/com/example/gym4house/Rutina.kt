package com.example.gym4house

import com.google.firebase.firestore.DocumentId

data class Rutina(
    @DocumentId var id: String = "", // <--- ¡CAMBIADO A 'var' AQUÍ!
    val nombreRutina: String = "",
    val descripcion: String = "",
    val nivel: String = "",
    val duracionMinutos: Long = 0,
    val tipo: String = "",
    val ejercicios: List<Map<String, Any>> = emptyList()
) {
    constructor() : this("", "", "", "", 0, "", emptyList())
}