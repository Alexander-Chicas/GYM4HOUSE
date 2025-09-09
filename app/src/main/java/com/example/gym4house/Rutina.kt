package com.example.gym4house

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rutina(
    @DocumentId var id: String = "",
    val nombreRutina: String = "",
    val descripcion: String = "",
    val nivel: String = "",
    val duracionMinutos: Long = 0,
    val tipo: String = "",
    // ¡Aquí está el cambio clave!
    val ejercicios: List<Ejercicio> = emptyList(),
    val equipamiento: List<String>? = null
) : Parcelable {
    constructor() : this("", "", "", "", 0, "", emptyList())

    companion object {
    }
}