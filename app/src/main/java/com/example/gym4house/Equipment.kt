package com.example.gym4house

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Equipment(
    val name: String,
    var isSelected: Boolean
) : Parcelable