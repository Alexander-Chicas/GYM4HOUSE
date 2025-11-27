package com.example.gym4house

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",

    // CAMBIO CLAVE: Volvemos a String porque así está en tu Firebase
    val ingredients: String = "",
    val instructions: String = "",

    val mealTime: String = "",
    val goal: String = "",

    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0
) : Parcelable