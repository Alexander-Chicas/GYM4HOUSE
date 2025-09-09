package com.example.gym4house

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ejercicio(
    val nombreEjercicio: String = "",
    val repeticiones: Int = 0,
    val series: Int = 0,
    val descansoSegundos: Int = 0,
    val descripcion: String = "",
    val mediaUrl: String? = null // 🔹 URL en Firebase Storage o servidor
) : Parcelable
