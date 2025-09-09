package com.example.gym4house

import java.io.Serializable

data class Recipe(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val mealTime: String = "",
    val goal: String = ""
) : Serializable
