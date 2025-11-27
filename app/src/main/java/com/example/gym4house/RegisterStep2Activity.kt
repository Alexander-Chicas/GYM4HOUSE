package com.example.gym4house

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityRegisterStep2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterStep2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Quitar barra de estado (Diseño Dark Glass)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityRegisterStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // 3. Configurar Spinners
        setupDropdowns()

        // 4. Botones
        binding.buttonFinalizar.setOnClickListener {
            procesarRegistro()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupDropdowns() {
        // Llenar los menús desplegables con los arrays de strings.xml
        val objetivos = resources.getStringArray(R.array.objetivos_usuario_array)
        val adapterObj = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, objetivos)
        binding.autoCompleteObjetivo.setAdapter(adapterObj)


        val niveles = resources.getStringArray(R.array.niveles_rutina_array)
        binding.autoCompleteExperiencia.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, niveles))

        val actividad = resources.getStringArray(R.array.activity_level_options)
        binding.autoCompleteActividad.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, actividad))

        val ejercicios = resources.getStringArray(R.array.exercise_type_options)
        val adapterEjercicios = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ejercicios)
        binding.autoCompleteTipoEjercicio.setAdapter(adapterEjercicios)
    }

    private fun procesarRegistro() {
        // A. RECUPERAR DATOS DEL PASO 1
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido = intent.getStringExtra("apellido") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val imageUriString = intent.getStringExtra("imageUri")

        // B. OBTENER DATOS DEL PASO 2
        val edadStr = binding.editTextEdad.text.toString().trim()
        val pesoStr = binding.editTextPeso.text.toString().trim()
        val alturaStr = binding.editTextAltura.text.toString().trim()
        val objetivo = binding.autoCompleteObjetivo.text.toString()

        // Validaciones rápidas
        if (edadStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty()) {
            Toast.makeText(this, "Faltan datos físicos", Toast.LENGTH_SHORT).show()
            return
        }
        if (objetivo.isEmpty() || objetivo == "Objetivo Principal") {
            Toast.makeText(this, "Selecciona un objetivo", Toast.LENGTH_SHORT).show()
            return
        }

        // Bloquear botón
        binding.buttonFinalizar.isEnabled = false
        binding.buttonFinalizar.text = "Guardando..."

        // C. CREAR USUARIO EN AUTH
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // --- AQUÍ ESTÁ LA CORRECCIÓN ---
                    // Definimos explícitamente el tipo HashMap<String, Any>
                    val userMap: HashMap<String, Any> = hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "email" to email,
                        "edad" to edadStr.toInt(),
                        "peso" to pesoStr.toDouble(),
                        "altura" to alturaStr.toDouble(),
                        "objetivo" to objetivo,
                        "rol" to "usuario"
                    )

                    // D. ¿SUBIR FOTO O GUARDAR?
                    if (!imageUriString.isNullOrEmpty()) {
                        subirFotoYGuardarPerfil(uid, Uri.parse(imageUriString), userMap)
                    } else {
                        guardarEnFirestore(uid, userMap)
                    }

                } else {
                    binding.buttonFinalizar.isEnabled = true
                    binding.buttonFinalizar.text = "Finalizar Registro"
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // --- FUNCIONES DE AYUDA ---

    private fun subirFotoYGuardarPerfil(uid: String, imageUri: Uri, userMap: HashMap<String, Any>) {
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    userMap["photoUrl"] = uri.toString()
                    guardarEnFirestore(uid, userMap)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir foto, guardando perfil...", Toast.LENGTH_SHORT).show()
                guardarEnFirestore(uid, userMap)
            }
    }

    private fun guardarEnFirestore(uid: String, userMap: HashMap<String, Any>) {
        db.collection("usuarios").document(uid).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                // IR A RESTRICCIONES DE SALUD
                val intent = Intent(this, HealthRestrictionsActivity::class.java)
                intent.putExtra(HealthRestrictionsActivity.LAUNCH_MODE_EXTRA, HealthRestrictionsActivity.MODE_REGISTER)
                startActivity(intent)

                finishAffinity()
            }
            .addOnFailureListener {
                binding.buttonFinalizar.isEnabled = true
                Toast.makeText(this, "Error al guardar en BD", Toast.LENGTH_SHORT).show()
            }
    }
}