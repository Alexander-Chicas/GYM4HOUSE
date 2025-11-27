package com.example.gym4house

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gym4house.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Variable para guardar la nueva foto si el usuario la cambia
    private var newImageUri: Uri? = null

    // Lanzador para abrir la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            // Mostrar la nueva foto inmediatamente
            binding.ivProfileImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setupDropdowns()
        loadUserData()
        setupListeners()
    }

    private fun setupDropdowns() {
        val context = requireContext()
        // Llenar los menús con las opciones de strings.xml
        val objetivos = resources.getStringArray(R.array.objetivos_usuario_array)
        binding.autoCompleteObjetivo.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, objetivos))

        val niveles = resources.getStringArray(R.array.niveles_rutina_array)
        binding.autoCompleteExperiencia.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, niveles))

        val actividad = resources.getStringArray(R.array.activity_level_options)
        binding.autoCompleteActividad.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, actividad))

        val ejercicios = resources.getStringArray(R.array.exercise_type_options)
        binding.autoCompleteTipoEjercicio.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, ejercicios))
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        binding.editTextPerfilEmail.setText(auth.currentUser?.email)

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Cargar Textos
                    binding.editTextPerfilNombre.setText(document.getString("nombre"))
                    binding.editTextPerfilEdad.setText(document.getLong("edad")?.toString())
                    binding.editTextPerfilPeso.setText(document.getDouble("peso")?.toString())
                    binding.editTextPerfilAltura.setText(document.getDouble("altura")?.toString())

                    // Cargar Dropdowns (false para que no se desplieguen solos)
                    binding.autoCompleteObjetivo.setText(document.getString("objetivo"), false)
                    binding.autoCompleteExperiencia.setText(document.getString("nivelExperiencia"), false)
                    binding.autoCompleteActividad.setText(document.getString("nivelActividad"), false)
                    binding.autoCompleteTipoEjercicio.setText(document.getString("tipoEjercicio"), false)

                    // Cargar Foto con Glide
                    val photoUrl = document.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop() // Recorte circular
                            .placeholder(R.drawable.ic_person)
                            .into(binding.ivProfileImage)
                    }

                    // Cargar Nivel en el texto naranja (Opcional: Lógica visual)
                    val nivel = document.getString("nivelExperiencia") ?: "Principiante"
                    binding.tvUserLevel.text = "Nivel: $nivel"
                }
            }
    }

    private fun setupListeners() {
        // 1. Cambiar Foto (Clic en la imagen)
        binding.ivProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 2. Ir a Ajustes (Engranaje)
        binding.btnSettings.setOnClickListener {
            // Navegar al fragmento de Ajustes (lo crearemos después)
            (activity as? MainActivity)?.replaceFragment(SettingsFragment())
        }

        // 3. Guardar Cambios
        binding.buttonGuardarCambios.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val uid = auth.currentUser?.uid ?: return
        binding.buttonGuardarCambios.isEnabled = false
        binding.buttonGuardarCambios.text = "Guardando..."

        // Datos básicos
        val updates = hashMapOf<String, Any>(
            "nombre" to binding.editTextPerfilNombre.text.toString(),
            "edad" to (binding.editTextPerfilEdad.text.toString().toIntOrNull() ?: 0),
            "peso" to (binding.editTextPerfilPeso.text.toString().toDoubleOrNull() ?: 0.0),
            "altura" to (binding.editTextPerfilAltura.text.toString().toDoubleOrNull() ?: 0.0),
            "objetivo" to binding.autoCompleteObjetivo.text.toString(),
            "nivelExperiencia" to binding.autoCompleteExperiencia.text.toString(),
            "nivelActividad" to binding.autoCompleteActividad.text.toString(),
            "tipoEjercicio" to binding.autoCompleteTipoEjercicio.text.toString()
        )

        // ¿El usuario cambió la foto?
        if (newImageUri != null) {
            // Subir foto nueva primero
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(newImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        updates["photoUrl"] = uri.toString()
                        updateFirestore(uid, updates)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al subir foto", Toast.LENGTH_SHORT).show()
                    updateFirestore(uid, updates) // Guardar datos aunque falle la foto
                }
        } else {
            // Solo actualizar datos
            updateFirestore(uid, updates)
        }
    }

    private fun updateFirestore(uid: String, updates: HashMap<String, Any>) {
        db.collection("usuarios").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "¡Perfil Actualizado!", Toast.LENGTH_SHORT).show()
                binding.buttonGuardarCambios.isEnabled = true
                binding.buttonGuardarCambios.text = "Guardar Cambios"
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                binding.buttonGuardarCambios.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}