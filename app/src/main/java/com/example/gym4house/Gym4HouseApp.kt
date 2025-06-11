package com.example.gym4house

import android.app.Application
import com.google.firebase.FirebaseApp

class Gym4HouseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}