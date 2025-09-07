package com.example.gym4house

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ejercicio(
    val nombreEjercicio: String = "",
    val repeticiones: Long = 0,
    val series: Long = 0,
    val descansoSegundos: Long = 0
) : Parcelable {
    // Constructor sin argumentos requerido por Firestore para la deserialización
    constructor() : this("", 0, 0, 0)
}