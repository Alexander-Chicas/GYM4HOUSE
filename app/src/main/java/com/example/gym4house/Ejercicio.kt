package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

// Importa FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

data class Ejercicio(
    val nombreEjercicio: String = "",
    val series: Long = 0, // Usamos Long para números de Firestore
    val repeticiones: Long = 0,
    val descansoSegundos: Long = 0,
    val urlVideo: String = "", // Opcional
    val instrucciones: String = "" // Opcional
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    constructor() : this("", 0, 0, 0, "", "")

    // Función de ayuda para convertir un Map (como viene de Firestore) a objeto Ejercicio
    companion object {
        fun fromMap(map: Map<String, Any>): Ejercicio {
            return Ejercicio(
                nombreEjercicio = map["nombreEjercicio"] as? String ?: "",
                series = map["series"] as? Long ?: 0,
                repeticiones = map["repeticiones"] as? Long ?: 0,
                descansoSegundos = map["descansoSegundos"] as? Long ?: 0,
                urlVideo = map["urlVideo"] as? String ?: "",
                instrucciones = map["instrucciones"] as? String ?: ""
            )
        }
    }
}