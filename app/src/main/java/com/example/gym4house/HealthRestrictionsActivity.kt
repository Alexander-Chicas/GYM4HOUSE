package com.example.gym4house

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityHealthRestrictionsBinding

class HealthRestrictionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthRestrictionsBinding

    /**
     * Objeto complementario para definir constantes de modos de lanzamiento.
     */
    companion object {
        const val LAUNCH_MODE_EXTRA = "launch_mode" // Clave para el extra en el Intent
        const val MODE_REGISTER = "register"         // Modo: flujo de registro inicial
        const val MODE_EDIT_PROFILE = "edit_profile" // Modo: edición desde el perfil
        // REMOVIDO: const val NAVIGATE_TO_PROFILE_EXTRA = "navigate_to_profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthRestrictionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = HealthRestrictionsFragment()
            val bundle = Bundle()

            // Recoger el modo de lanzamiento del Intent que inició esta Activity.
            // Si no se proporciona el extra, asumimos que es para edición de perfil por defecto.
            val launchMode = intent.getStringExtra(LAUNCH_MODE_EXTRA) ?: MODE_EDIT_PROFILE
            bundle.putString(LAUNCH_MODE_EXTRA, launchMode) // Guardar el modo en el Bundle
            fragment.arguments = bundle                     // Asignar el Bundle al fragmento

            // Cargar el HealthRestrictionsFragment en el contenedor de la Activity.
            supportFragmentManager.beginTransaction()
                .replace(R.id.health_restrictions_fragment_container, fragment)
                .commit()
        }
    }
}