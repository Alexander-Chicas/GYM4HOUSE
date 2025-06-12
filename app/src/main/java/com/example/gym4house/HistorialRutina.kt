package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import com.google.firebase.Timestamp // Importa Timestamp para la fecha

data class HistorialRutina(
    val id: String = "", // El ID del documento del historial en Firestore
    val rutinaId: String = "", // Opcional: si quisieras enlazar con una rutina específica
    val nombreRutina: String = "",
    val fechaCompletado: Timestamp = Timestamp.now(), // Fecha y hora de completado
    val duracionMinutos: Long = 0, // Duración de la rutina que se completó
    val nivel: String = "" // Nivel de la rutina que se completó
    // Puedes añadir más campos como 'caloriasQuemadas', 'notas', 'ejerciciosRealizados', etc.
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    constructor() : this("", "", "", Timestamp.now(), 0, "")
}