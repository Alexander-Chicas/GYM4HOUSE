package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.data.dao.PerfilClienteDao
import com.example.gym4house.data.dao.UsuarioDao
import com.example.gym4house.data.entity.PerfilCliente
import com.example.gym4house.data.entity.Usuario
import com.example.gym4house.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var perfilClienteDao: PerfilClienteDao
    private lateinit var usuarioDao: UsuarioDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Inicializar DAOs (asume que ya tienes una base de datos Room configurada)
        // perfilClienteDao = AppDatabase.getDatabase(requireContext()).perfilClienteDao()
        // usuarioDao = AppDatabase.getDatabase(requireContext()).usuarioDao()

        setupSpinners()
        loadUserProfile()
        setupListeners()
    }

    private fun setupSpinners() {
        val experienceAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.experience_level_options,
            android.R.layout.simple_spinner_item
        )
        experienceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPerfilNivelExperiencia.adapter = experienceAdapter

        val goalAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.goal_options,
            android.R.layout.simple_spinner_item
        )
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPerfilObjetivo.adapter = goalAdapter

        val exerciseTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.exercise_type_options,
            android.R.layout.simple_spinner_item
        )
        exerciseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPerfilTipoEjercicios.adapter = exerciseTypeAdapter
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            binding.editTextPerfilEmail.setText(currentUser.email)

            firestore.collection("usuarios").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.editTextPerfilNombre.setText(document.getString("nombre") ?: "")
                        binding.editTextPerfilEdad.setText(document.getLong("edad")?.toString() ?: "")
                        binding.editTextPerfilAltura.setText(document.getDouble("altura")?.toString() ?: "")
                        binding.editTextPerfilPeso.setText(document.getDouble("peso")?.toString() ?: "")

                        val experienceLevel = document.getString("nivel_experiencia")
                        val goal = document.getString("objetivo_principal")
                        val exerciseType = document.getString("tipo_ejercicio_preferido")

                        setSelectedSpinnerItem(binding.spinnerPerfilNivelExperiencia, experienceLevel)
                        setSelectedSpinnerItem(binding.spinnerPerfilObjetivo, goal)
                        setSelectedSpinnerItem(binding.spinnerPerfilTipoEjercicios, exerciseType)

                    } else {
                        Toast.makeText(requireContext(), "Datos de perfil no encontrados.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setSelectedSpinnerItem(spinner: android.widget.Spinner, value: String?) {
        if (value != null) {
            val adapter = spinner.adapter as ArrayAdapter<String>
            val spinnerPosition = adapter.getPosition(value)
            if (spinnerPosition != -1) {
                spinner.setSelection(spinnerPosition)
            }
        }
    }

    private fun setupListeners() {
        binding.buttonGuardarCambios.setOnClickListener {
            saveUserProfile()
        }

        binding.buttonCambiarPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.buttonConfigurarRecordatorios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RemindersSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.buttonCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(activity, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun saveUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userProfile = hashMapOf(
                "nombre" to binding.editTextPerfilNombre.text.toString(),
                "edad" to binding.editTextPerfilEdad.text.toString().toLongOrNull(),
                "altura" to binding.editTextPerfilAltura.text.toString().toDoubleOrNull(),
                "peso" to binding.editTextPerfilPeso.text.toString().toDoubleOrNull(),
                "nivel_experiencia" to binding.spinnerPerfilNivelExperiencia.selectedItem.toString(),
                "objetivo_principal" to binding.spinnerPerfilObjetivo.selectedItem.toString(),
                "tipo_ejercicio_preferido" to binding.spinnerPerfilTipoEjercicios.selectedItem.toString()
            )

            firestore.collection("usuarios").document(userId)
                .update(userProfile as Map<String, Any>) // Cast for Firebase
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al actualizar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}