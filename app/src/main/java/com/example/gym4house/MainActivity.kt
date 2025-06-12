package com.example.gym4house

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.app.NotificationChannel // Importar
import android.app.NotificationManager // Importar
import android.content.Context // Importar
import android.os.Build // Importar para verificar la versión de Android

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // CREAR EL CANAL DE NOTIFICACIÓN (Añadir este bloque, idealmente al principio)
        createNotificationChannel()

        bottomNavigationView = findViewById(R.id.bottom_navigation) // Obtener referencia al BottomNavigationView

        // Configurar el listener para la selección de ítems en la barra de navegación
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment()) // Cargar HomeFragment
                    true
                }
                R.id.navigation_rutinas -> {
                    replaceFragment(RutinasFragment()) // Cargar RutinasFragment (US-02)
                    true
                }
                R.id.navigation_perfil -> {
                    replaceFragment(PerfilFragment()) // Cargar PerfilFragment
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento de inicio por defecto cuando la actividad se crea
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    // FUNCIÓN PARA CREAR EL CANAL DE NOTIFICACIÓN (Añadir esta función completa)
    private fun createNotificationChannel() {
        // Solo se necesita en API 26+ (Android 8.0 Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Entrenamiento" // Nombre que verá el usuario en la configuración del sistema
            val descriptionText = "Notificaciones para recordar tus sesiones de entrenamiento." // Descripción para el usuario
            val importance = NotificationManager.IMPORTANCE_HIGH // Nivel de importancia (HIGH hará ruido y aparecerá en la parte superior)
            val channel = NotificationChannel("gym4house_reminders", name, importance).apply {
                description = descriptionText
                // Puedes añadir más configuraciones aquí, como habilitar luces, vibración, etc.
                enableLights(true)
                lightColor = R.color.gym_brown_primary // Usa tu color marrón definido en colors.xml
                enableVibration(true)
                // vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100) // Patrón de vibración
            }
            // Registrar el canal con el sistema Android
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Función para reemplazar el fragmento en el contenedor
    // ¡CAMBIO AQUÍ! Eliminando 'private' para que sea accesible desde otros fragmentos
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment) // Asegúrate de que R.id.fragment_container es el ID correcto
            commit()
        }
    }
}