package com.example.gym4house

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.gym4house.ProgessiveFragment // <-- ¡Nueva importación!

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Manejar navegación inicial
        if (auth.currentUser == null) {
            replaceFragment(LoginFragment())
        } else {
            replaceFragment(HomeFragment())
        }

        // Conectar el BottomNavigationView con los fragments
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_rutinas -> {
                    replaceFragment(RutinasFragment())
                    true
                }
                R.id.navigation_progreso -> { // <-- ¡Nuevo caso para Progreso!
                    replaceFragment(ProgessiveFragment())
                    true
                }
                R.id.navigation_perfil -> {
                    replaceFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    fun replaceFragment(fragment: Fragment) {
        // Controla la visibilidad de la barra de navegación basado en el tipo de fragmento
        if (fragment is LoginFragment) {
            showBottomNav(false)
        }
        else {
            showBottomNav(true)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Muestra u oculta la barra de navegación inferior.
     * @param visible True para mostrar, false para ocultar.
     */
    fun showBottomNav(visible: Boolean) {
        binding.bottomNavigation.visibility = if (visible) View.VISIBLE else View.GONE
    }
}