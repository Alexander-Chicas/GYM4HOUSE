package com.example.gym4house

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val KEY_ACTIVE_FRAGMENT = "active_fragment"
    }

    private var activeFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Aplicar modo oscuro según SharedPreferences
        val darkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Ajustar padding por barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Restaurar fragmento activo después de recreación
        activeFragmentTag = savedInstanceState?.getString(KEY_ACTIVE_FRAGMENT)
        if (activeFragmentTag != null) {
            val fragment = supportFragmentManager.findFragmentByTag(activeFragmentTag)
            if (fragment != null) {
                replaceFragment(fragment, false)
            } else {
                replaceFragment(HomeFragment())
            }
        } else {
            replaceFragment(HomeFragment())
        }

        // Conectar BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_rutinas -> RutinasFragment()
                R.id.navigation_progreso -> ProgessiveFragment()
                R.id.navigation_recetas -> HealthyRecipesFragment()
                R.id.navigation_perfil -> PerfilFragment()
                else -> return@setOnItemSelectedListener false
            }
            replaceFragment(fragment)
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        activeFragmentTag?.let { outState.putString(KEY_ACTIVE_FRAGMENT, it) }
    }

    /**
     * Reemplaza el fragmento actual y controla la visibilidad del BottomNavigationView.
     */
    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        activeFragmentTag = fragment.javaClass.simpleName
        showBottomNav(fragment !is LoginFragment)

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, activeFragmentTag)

        if (addToBackStack) transaction.addToBackStack(activeFragmentTag)
        transaction.commit()
    }

    /**
     * Muestra u oculta la barra de navegación inferior.
     * @param visible True para mostrar, false para ocultar.
     */
    fun showBottomNav(visible: Boolean) {
        binding.bottomNavigation.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Cambia el modo oscuro y guarda la preferencia.
     */
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
