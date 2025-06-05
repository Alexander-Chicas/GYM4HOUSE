package com.example.gym4house // Asegúrate de que el paquete sea el correcto

import android.os.Bundle
import android.util.Log // Importa la clase Log para ver mensajes en Logcat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Necesario para coroutines en Activities
import com.example.gym4house.data.db.AppDatabase // Importa tu AppDatabase
import com.example.gym4house.data.entity.Usuario // Importa tu entidad Usuario
import com.example.gym4house.data.entity.PerfilCliente // Importa tu entidad PerfilCliente
import kotlinx.coroutines.launch // Importa la función launch para coroutines

class MainActivity : AppCompatActivity() {

    private lateinit var appDatabase: AppDatabase // Declaramos una variable para la instancia de la DB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate de tener un layout principal

    }
}