package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Declarar los Spinners
    private lateinit var spinnerObjetivo: Spinner
    private lateinit var spinnerNivelExperiencia: Spinner
    private lateinit var spinnerTipoEjercicioPreferido: Spinner // <--- NUEVA DECLARACIÓN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar los Spinners usando findViewById
        spinnerObjetivo = findViewById(R.id.spinnerObjetivo)
        spinnerNivelExperiencia = findViewById(R.id.spinnerNivelExperiencia)
        spinnerTipoEjercicioPreferido = findViewById(R.id.spinnerTipoEjercicioPreferido) // <--- NUEVA INICIALIZACIÓN

        // Configurar adaptadores para los Spinners
        val objetivosAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.objetivos_usuario_array,
            android.R.layout.simple_spinner_item
        )
        objetivosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerObjetivo.adapter = objetivosAdapter

        val nivelesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.niveles_rutina_array,
            android.R.layout.simple_spinner_item
        )
        nivelesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNivelExperiencia.adapter = nivelesAdapter

        // <--- NUEVA CONFIGURACIÓN DE ADAPTADOR PARA TIPO DE EJERCICIO
        val tipoEjercicioAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.exercise_type_options,
            android.R.layout.simple_spinner_item
        )
        tipoEjercicioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEjercicioPreferido.adapter = tipoEjercicioAdapter
        // -------------------------------------------------------------

        binding.buttonRegistrar.setOnClickListener {
            Log.d("LoginActivity", "Botón de Registrar clicado.")
            performRegistration()
        }

        binding.textViewGoToSignIn.setOnClickListener {
            Log.d("LoginActivity", "Go to SignIn TextView clicado.")
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performRegistration() {
        val name = binding.editTextNombre.text.toString().trim()
        val lastName = binding.editTextApellido.text.toString().trim()
        val email = binding.editTextCorreo.text.toString().trim()
        val password = binding.editTextContrasena.text.toString().trim()
        val confirmPassword = binding.editTextConfirmarContrasena.text.toString().trim()
        val role = binding.editTextRol.text.toString().trim()

        val edadStr = binding.editTextEdad.text.toString().trim()
        val pesoStr = binding.editTextPeso.text.toString().trim()
        val alturaStr = binding.editTextAltura.text.toString().trim()

        // Obtener valores de los Spinners
        val objetivo = spinnerObjetivo.selectedItem.toString()
        val nivelExperiencia = spinnerNivelExperiencia.selectedItem.toString()
        val tipoEjercicioPreferido = spinnerTipoEjercicioPreferido.selectedItem.toString() // <--- OBTENER VALOR

        // Validaciones de campos EditText
        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || role.isEmpty() || edadStr.isEmpty() || pesoStr.isEmpty() ||
            alturaStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos para registrarte.", Toast.LENGTH_LONG).show()
            Log.d("LoginActivity", "Validación: Campos vacíos (EditTexts).")
            return
        }

        // Validación específica para Spinners
        if (objetivo == "Selecciona tu Objetivo") {
            Toast.makeText(this, "Por favor, selecciona tu objetivo.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Objetivo no seleccionado.")
            return
        }
        if (nivelExperiencia == "Todos los Niveles") {
            Toast.makeText(this, "Por favor, selecciona tu nivel de experiencia.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Nivel de experiencia no seleccionado.")
            return
        }
        // <--- NUEVA VALIDACIÓN PARA TIPO DE EJERCICIO PREFERIDO
        if (tipoEjercicioPreferido == "Selecciona un Tipo de Ejercicio") { // Asumiendo este es el texto por defecto/hint
            Toast.makeText(this, "Por favor, selecciona tu tipo de ejercicio preferido.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Tipo de ejercicio preferido no seleccionado.")
            return
        }
        // -------------------------------------------------------------

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Correo inválido.")
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Contraseñas no coinciden.")
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Contraseña corta.")
            return
        }
        if (role.length < 3) {
            Toast.makeText(this, "El rol debe ser válido (ej. Usuario, Entrenador).", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Rol inválido.")
            return
        }

        val edad: Int
        val peso: Double
        val altura: Double
        try {
            edad = edadStr.toInt()
            peso = pesoStr.toDouble()
            altura = alturaStr.toDouble()
            Log.d("LoginActivity", "Validación: Conversión numérica exitosa.")
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Edad, Peso y Altura deben ser números válidos.", Toast.LENGTH_SHORT).show()
            Log.e("LoginActivity", "Validación: Error de conversión numérica", e)
            return
        }

        Log.d("LoginActivity", "Intentando crear usuario con Firebase Auth...")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Registro de Auth exitoso.")
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        Log.d("LoginActivity", "UID obtenido: $uid. Intentando guardar en Firestore...")
                        val userProfile = hashMapOf(
                            "uid" to uid,
                            "nombre" to name,
                            "apellido" to lastName,
                            "email" to email,
                            "rol" to role.toLowerCase(),
                            "edad" to edad,
                            "peso" to peso,
                            "altura" to altura,
                            "objetivo" to objetivo,
                            "nivelExperiencia" to nivelExperiencia,
                            "tipo_ejercicio_preferido" to tipoEjercicioPreferido // <--- GUARDAR EN FIRESTORE
                        )

                        db.collection("usuarios")
                            .document(uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso. Continuemos con las restricciones de salud.", Toast.LENGTH_LONG).show()
                                Log.d("LoginActivity", "Perfil guardado en Firestore. Redirigiendo a HealthRestrictionsActivity...")
                                val intent = Intent(this, HealthRestrictionsActivity::class.java)
                                intent.putExtra(HealthRestrictionsActivity.LAUNCH_MODE_EXTRA, HealthRestrictionsActivity.MODE_REGISTER)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Registro exitoso, pero fallo al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "Fallo al guardar perfil en Firestore. Redirigiendo a HealthRestrictionsActivity...", e)
                                val intent = Intent(this, HealthRestrictionsActivity::class.java)
                                intent.putExtra(HealthRestrictionsActivity.LAUNCH_MODE_EXTRA, HealthRestrictionsActivity.MODE_REGISTER)
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        Log.e("LoginActivity", "Registro de Auth exitoso, pero UID es nulo.", task.exception)
                        Toast.makeText(this, "Registro exitoso, pero UID de usuario no encontrado.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }
}