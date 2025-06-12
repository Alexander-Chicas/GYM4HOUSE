package com.example.gym4house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
// import com.google.android.material.switchmaterial.SwitchMaterial // Ya no es necesario si quitamos el Switch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // TextInputEditTexts para editar la información del usuario
    private lateinit var editTextNombre: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextEdad: TextInputEditText
    private lateinit var editTextAltura: TextInputEditText
    private lateinit var editTextPeso: TextInputEditText

    // Spinners para Nivel de Experiencia y Objetivo
    private lateinit var spinnerNivelExperiencia: Spinner
    private lateinit var spinnerObjetivo: Spinner

    // PREFERENCIAS (Solo tipo de ejercicios, Idioma y Notificaciones eliminados)
    private lateinit var spinnerTipoEjercicios: Spinner // Spinner para Tipo de Ejercicios

    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonCambiarPassword: Button
    private lateinit var buttonConfigurarRecordatorios: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializar los TextInputEditTexts
        editTextNombre = view.findViewById(R.id.editTextPerfilNombre)
        editTextEmail = view.findViewById(R.id.editTextPerfilEmail)
        editTextEdad = view.findViewById(R.id.editTextPerfilEdad)
        editTextAltura = view.findViewById(R.id.editTextPerfilAltura)
        editTextPeso = view.findViewById(R.id.editTextPerfilPeso)

        // Inicializar los Spinners de perfil existentes
        spinnerNivelExperiencia = view.findViewById(R.id.spinnerPerfilNivelExperiencia)
        spinnerObjetivo = view.findViewById(R.id.spinnerPerfilObjetivo)

        // Inicializar el Spinner de Tipo de Ejercicios (Idioma y Notificaciones eliminados)
        spinnerTipoEjercicios = view.findViewById(R.id.spinnerPerfilTipoEjercicios)

        // Configurar adaptadores para los Spinners existentes
        val nivelesAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.niveles_rutina_array,
            android.R.layout.simple_spinner_item
        )
        nivelesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNivelExperiencia.adapter = nivelesAdapter

        val objetivosAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.objetivos_usuario_array,
            android.R.layout.simple_spinner_item
        )
        objetivosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerObjetivo.adapter = objetivosAdapter

        // Configurar adaptador para el Spinner de Tipo de Ejercicios
        val tiposEjerciciosAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_rutina_array,
            android.R.layout.simple_spinner_item
        )
        tiposEjerciciosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEjercicios.adapter = tiposEjerciciosAdapter


        buttonGuardarCambios = view.findViewById(R.id.buttonGuardarCambios)
        buttonCambiarPassword = view.findViewById(R.id.buttonCambiarPassword)
        buttonConfigurarRecordatorios = view.findViewById(R.id.buttonConfigurarRecordatorios)

        loadUserProfile() // Cargar los datos del perfil al inicio

        buttonGuardarCambios.setOnClickListener {
            saveUserProfile() // Guardar los cambios
        }

        buttonCambiarPassword.setOnClickListener {
            sendPasswordResetEmail() // Enviar correo de restablecimiento de contraseña
        }

        buttonConfigurarRecordatorios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecordatoriosFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

        if (userId == null) {
            Toast.makeText(context, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            editTextNombre.setText("")
            editTextEmail.setText("")
            spinnerNivelExperiencia.setSelection(0)
            spinnerObjetivo.setSelection(0)
            spinnerTipoEjercicios.setSelection(0) // Reset tipo ejercicios
            editTextEdad.setText("")
            editTextAltura.setText("")
            editTextPeso.setText("")
            return
        }

        editTextEmail.setText(userEmail ?: "")

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    editTextNombre.setText(document.getString("nombre") ?: "")
                    editTextEdad.setText(document.getLong("edad")?.toString() ?: "")
                    editTextAltura.setText(document.getLong("altura")?.toString() ?: "")
                    editTextPeso.setText(document.getLong("peso")?.toString() ?: "")

                    // Cargar y seleccionar el valor en el Spinner de Nivel de Experiencia
                    val nivelExperiencia = document.getString("nivelExperiencia")
                    if (nivelExperiencia != null) {
                        val adapter = spinnerNivelExperiencia.adapter as ArrayAdapter<String>
                        val spinnerPosition = adapter.getPosition(nivelExperiencia)
                        if (spinnerPosition >= 0) {
                            spinnerNivelExperiencia.setSelection(spinnerPosition)
                        } else {
                            spinnerNivelExperiencia.setSelection(0)
                        }
                    } else {
                        spinnerNivelExperiencia.setSelection(0)
                    }

                    // Cargar y seleccionar el valor en el Spinner de Objetivo
                    val objetivo = document.getString("objetivo")
                    if (objetivo != null) {
                        val adapter = spinnerObjetivo.adapter as ArrayAdapter<String>
                        val spinnerPosition = adapter.getPosition(objetivo)
                        if (spinnerPosition >= 0) {
                            spinnerObjetivo.setSelection(spinnerPosition)
                        } else {
                            spinnerObjetivo.setSelection(0)
                        }
                    } else {
                        spinnerObjetivo.setSelection(0)
                    }

                    // Cargar y seleccionar el valor en el Spinner de Tipo de Ejercicios
                    val tipoEjercicio = document.getString("tipoEjercicio")
                    if (tipoEjercicio != null) {
                        val adapter = spinnerTipoEjercicios.adapter as ArrayAdapter<String>
                        val spinnerPosition = adapter.getPosition(tipoEjercicio)
                        if (spinnerPosition >= 0) {
                            spinnerTipoEjercicios.setSelection(spinnerPosition)
                        } else {
                            spinnerTipoEjercicios.setSelection(0)
                        }
                    } else {
                        spinnerTipoEjercicios.setSelection(0)
                    }

                    // Idioma y Notificaciones ya no se cargan

                } else {
                    Toast.makeText(context, "No se encontró el perfil de usuario en la base de datos.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cargar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "No hay usuario autenticado para guardar cambios.", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = editTextNombre.text.toString().trim()
        val edadStr = editTextEdad.text.toString().trim()
        val alturaStr = editTextAltura.text.toString().trim()
        val pesoStr = editTextPeso.text.toString().trim()

        // Obtener valores de los Spinners existentes
        val nivelExperiencia = spinnerNivelExperiencia.selectedItem.toString()
        val objetivo = spinnerObjetivo.selectedItem.toString()

        // Obtener valor del Spinner de Tipo de Ejercicios
        val tipoEjercicio = spinnerTipoEjercicios.selectedItem.toString()

        // Validaciones básicas para EditTexts
        if (nombre.isEmpty()) {
            editTextNombre.error = "El nombre es requerido"
            return
        }

        // Validaciones para Spinners existentes
        if (nivelExperiencia == "Todos los Niveles" || nivelExperiencia.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona tu nivel de experiencia.", Toast.LENGTH_SHORT).show()
            return
        }
        if (objetivo == "Selecciona tu Objetivo" || objetivo.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona tu objetivo.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validaciones para Spinner de Tipo de Ejercicios
        if (tipoEjercicio == "Selecciona Tipo de Ejercicio" || tipoEjercicio.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona tu tipo de ejercicio preferido.", Toast.LENGTH_SHORT).show()
            return
        }

        val edad = edadStr.toLongOrNull()
        val altura = alturaStr.toLongOrNull()
        val peso = pesoStr.toDoubleOrNull()

        if (edad == null || edad <= 0) {
            editTextEdad.error = "Edad inválida"
            return
        }
        if (altura == null || altura <= 0) {
            editTextAltura.error = "Altura inválida"
            return
        }
        if (peso == null || peso <= 0) {
            editTextPeso.error = "Peso inválido"
            return
        }

        val userUpdates = hashMapOf(
            "nombre" to nombre,
            "nivelExperiencia" to nivelExperiencia,
            "objetivo" to objetivo,
            "edad" to edad,
            "altura" to altura,
            "peso" to peso,
            "tipoEjercicio" to tipoEjercicio // Sólo este campo de las preferencias
        )

        firestore.collection("usuarios").document(userId)
            .update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun sendPasswordResetEmail() {
        val user = auth.currentUser
        val email = user?.email

        if (email != null) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Se ha enviado un correo de restablecimiento de contraseña a $email. Por favor, revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al enviar el correo de restablecimiento: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(context, "No se encontró un correo electrónico para enviar el restablecimiento de contraseña.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }
}