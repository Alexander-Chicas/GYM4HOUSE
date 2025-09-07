package com.example.gym4house

import com.google.firebase.Timestamp

data class HistorialRutina(
    val id: String = "",
    val rutinaId: String = "",
    val nombreRutina: String = "",
    val fechaCompletado: Timestamp = Timestamp.now(),
    val duracionMinutos: Long = 0,
    val nivel: String = "",
    // Restore the type to a map-based list for Firestore
    val ejerciciosRealizados: List<Map<String, Any>> = emptyList()
) {
    // Constructor without arguments is required for Firestore deserialization
    constructor() : this("", "", "", Timestamp.now(), 0, "", emptyList())
}