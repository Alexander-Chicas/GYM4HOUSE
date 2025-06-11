package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView // Importar BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    // Función para reemplazar el fragmento en el contenedor
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }
}