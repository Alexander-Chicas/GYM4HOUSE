package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    // Las propiedades perfilClienteDao y usuarioDao no se están utilizando y se han eliminado para simplificar.
    // Si necesitas Room en este fragmento, asegúrate de inicializarlas y usarlas.

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
        // Las DAOs relacionadas con Room se han comentado/eliminado ya que no se estaban usando.
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

                        val experienceLevel = document.getString("nivelExperiencia")
                        val goal = document.getString("objetivo")
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
            // Se mantiene la lógica de reemplazar fragmento si ChangePasswordFragment es un fragmento
            // dentro de MainActivity. Si fuera una Activity, lanzarías un Intent.
            (activity as? MainActivity)?.replaceFragment(ChangePasswordFragment())
        }

        binding.buttonConfigurarRecordatorios.setOnClickListener {
            // Se mantiene la lógica de reemplazar fragmento si RemindersSettingsFragment es un fragmento
            // dentro de MainActivity. Si fuera una Activity, lanzarías un Intent.
            (activity as? MainActivity)?.replaceFragment(RemindersSettingsFragment())
        }

        binding.buttonGestionarRestricciones.setOnClickListener {
            // *** ESTE ES EL CAMBIO CRÍTICO: Lanzar HealthRestrictionsActivity con un Intent ***
            val intent = Intent(requireContext(), HealthRestrictionsActivity::class.java)
            // No necesitamos enviar LAUNCH_MODE_EXTRA explícitamente a MODE_EDIT_PROFILE
            // porque es el valor por defecto en HealthRestrictionsActivity cuando no se especifica.
            startActivity(intent)
            Toast.makeText(requireContext(), "Gestionar Restricciones de Salud", Toast.LENGTH_SHORT).show()
        }

        // BOTÓN NUEVO: "Editar Equipamiento"
        binding.buttonEditarEquipamiento.setOnClickListener {
            val intent = Intent(requireContext(), EquipmentActivity::class.java)
            intent.putExtra(EquipmentActivity.LAUNCH_MODE_EXTRA, EquipmentActivity.MODE_EDIT_PROFILE)
            startActivity(intent)
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
                "nivelExperiencia" to binding.spinnerPerfilNivelExperiencia.selectedItem.toString(),
                "objetivo" to binding.spinnerPerfilObjetivo.selectedItem.toString(),
                "tipo_ejercicio_preferido" to binding.spinnerPerfilTipoEjercicios.selectedItem.toString()
            )

            firestore.collection("usuarios").document(userId)
                .update(userProfile as Map<String, Any>)
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
