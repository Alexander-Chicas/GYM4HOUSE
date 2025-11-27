package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Cargar nombre del usuario
        loadUserData()

        // 2. Configurar Clics
        setupClicks()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Busca el campo "nombre" en Firebase
                        val nombre = document.getString("nombre") ?: "Atleta"
                        binding.tvGreeting.text = "¡Hola, $nombre!"
                    }
                }
        }
    }

    private fun setupClicks() {
        // AHORA SÍ LOS ENCUENTRA PORQUE EL XML ES NUEVO:

        // Botón Perfil (Tarjeta)
        binding.btnPerfil.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(PerfilFragment())
        }

        // Foto de perfil (Header)
        binding.ivProfileHeader.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(PerfilFragment())
        }

        // Botón Rutinas
        binding.btnMisRutinas.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RutinasFragment())
        }

        // Botón Progreso
        binding.btnMiProgreso.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ProgessiveFragment())
        }

        // Botón Nutrición
        binding.btnRecetas.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(HealthyRecipesFragment())
        }

        // Botón Iniciar Entrenamiento (Tarjeta Grande)
        binding.btnPlayWorkout.setOnClickListener {
            Toast.makeText(context, "¡A entrenar!", Toast.LENGTH_SHORT).show()
            // Si tienes la pantalla de sesión, descomenta esto:
            // (activity as? MainActivity)?.replaceFragment(WorkoutSessionFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}