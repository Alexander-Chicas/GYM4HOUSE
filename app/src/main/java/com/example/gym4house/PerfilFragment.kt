package com.example.gym4house

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

        setupSpinners()
        loadUserProfile()
        setupListeners()
        setupDarkModeSwitch()
    }

    private fun setupSpinners() {
        listOf(
            binding.spinnerPerfilNivelExperiencia to R.array.experience_level_options,
            binding.spinnerPerfilObjetivo to R.array.objetivos_usuario_array,
            binding.spinnerPerfilTipoEjercicios to R.array.exercise_type_options,
            binding.spinnerPerfilNivelActividadFisica to R.array.activity_level_options
        ).forEach { (spinner, arrayRes) ->
            ArrayAdapter.createFromResource(
                requireContext(),
                arrayRes,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser ?: return
        binding.editTextPerfilEmail.setText(currentUser.email)

        firestore.collection("usuarios").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val b = _binding ?: return@addOnSuccessListener
                if (document.exists()) {
                    b.editTextPerfilNombre.setText(document.getString("nombre") ?: "")
                    b.editTextPerfilEdad.setText(document.getLong("edad")?.toString() ?: "")
                    b.editTextPerfilAltura.setText(document.getDouble("altura")?.toString() ?: "")
                    b.editTextPerfilPeso.setText(document.getDouble("peso")?.toString() ?: "")

                    setSelectedSpinnerItem(b.spinnerPerfilNivelExperiencia, document.getString("nivelExperiencia"))
                    setSelectedSpinnerItem(b.spinnerPerfilObjetivo, document.getString("objetivo"))
                    setSelectedSpinnerItem(b.spinnerPerfilTipoEjercicios, document.getString("tipo_ejercicio_preferido"))
                    setSelectedSpinnerItem(b.spinnerPerfilNivelActividadFisica, document.getString("nivelActividadFisica"))
                } else {
                    Toast.makeText(requireContext(), "Datos de perfil no encontrados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                val b = _binding ?: return@addOnFailureListener
                Toast.makeText(requireContext(), "Error al cargar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSelectedSpinnerItem(spinner: android.widget.Spinner, value: String?) {
        if (value != null) {
            val adapter = spinner.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(value)
            if (position != -1) spinner.setSelection(position)
        }
    }

    private fun setupListeners() {
        val b = _binding ?: return
        b.buttonGuardarCambios.setOnClickListener { saveUserProfile() }
        b.buttonCambiarPassword.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ChangePasswordFragment(), addToBackStack = false)
        }
        b.buttonConfigurarRecordatorios.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RemindersSettingsFragment())
        }
        b.buttonGestionarRestricciones.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), HealthRestrictionsActivity::class.java))
        }
        b.buttonEditarEquipamiento.setOnClickListener {
            val intent = android.content.Intent(requireContext(), EquipmentActivity::class.java)
            intent.putExtra(EquipmentActivity.LAUNCH_MODE_EXTRA, EquipmentActivity.MODE_EDIT)
            startActivity(intent)
        }
        b.buttonCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            val intent = android.content.Intent(activity, WelcomeActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun saveUserProfile() {
        val currentUser = firebaseAuth.currentUser ?: return
        val b = _binding ?: return

        val nivelActividad = b.spinnerPerfilNivelActividadFisica.selectedItem.toString()
        if (nivelActividad == getString(R.string.hint_activity_level)) {
            Toast.makeText(requireContext(), "Selecciona tu nivel de actividad física.", Toast.LENGTH_SHORT).show()
            return
        }

        val userProfile = hashMapOf(
            "nombre" to b.editTextPerfilNombre.text.toString(),
            "edad" to b.editTextPerfilEdad.text.toString().toLongOrNull(),
            "altura" to b.editTextPerfilAltura.text.toString().toDoubleOrNull(),
            "peso" to b.editTextPerfilPeso.text.toString().toDoubleOrNull(),
            "nivelExperiencia" to b.spinnerPerfilNivelExperiencia.selectedItem.toString(),
            "objetivo" to b.spinnerPerfilObjetivo.selectedItem.toString(),
            "tipo_ejercicio_preferido" to b.spinnerPerfilTipoEjercicios.selectedItem.toString(),
            "nivelActividadFisica" to nivelActividad
        )

        firestore.collection("usuarios").document(currentUser.uid)
            .update(userProfile as Map<String, Any>)
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                Toast.makeText(requireContext(), "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(requireContext(), "Error al actualizar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDarkModeSwitch() {
        val mainActivity = activity as? MainActivity ?: return
        val b = _binding ?: return

        val isDarkMode = mainActivity.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        b.switchDarkMode.isChecked = isDarkMode

        b.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            mainActivity.setDarkMode(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
