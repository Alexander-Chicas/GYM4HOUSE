package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.util.Log // Importa Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.buttonRegistrar.setOnClickListener {
            Log.d("LoginActivity", "Botón de Registrar clicado.") // Log
            performRegistration()
        }

        binding.textViewGoToSignIn.setOnClickListener {
            Log.d("LoginActivity", "Go to SignIn TextView clicado.") // Log
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
        val objetivo = binding.editTextObjetivo.text.toString().trim()
        val nivelExperiencia = binding.editTextNivelExperiencia.text.toString().trim()


        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty() || role.isEmpty() || edadStr.isEmpty() || pesoStr.isEmpty() ||
            alturaStr.isEmpty() || objetivo.isEmpty() || nivelExperiencia.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos para registrarte.", Toast.LENGTH_LONG).show()
            Log.d("LoginActivity", "Validación: Campos vacíos.") // Log
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Correo inválido.") // Log
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Contraseñas no coinciden.") // Log
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Contraseña corta.") // Log
            return
        }
        if (role.length < 3) {
            Toast.makeText(this, "El rol debe ser válido (ej. Usuario, Entrenador).", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity", "Validación: Rol inválido.") // Log
            return
        }

        val edad: Int
        val peso: Double
        val altura: Double
        try {
            edad = edadStr.toInt()
            peso = pesoStr.toDouble()
            altura = alturaStr.toDouble()
            Log.d("LoginActivity", "Validación: Conversión numérica exitosa.") // Log
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Edad, Peso y Altura deben ser números válidos.", Toast.LENGTH_SHORT).show()
            Log.e("LoginActivity", "Validación: Error de conversión numérica", e) // Log de error
            return
        }

        Log.d("LoginActivity", "Intentando crear usuario con Firebase Auth...") // Log
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Registro de Auth exitoso.") // Log
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        Log.d("LoginActivity", "UID obtenido: $uid. Intentando guardar en Firestore...") // Log
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
                            "nivelExperiencia" to nivelExperiencia
                        )

                        db.collection("usuarios")
                            .document(uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso. ¡Bienvenido!", Toast.LENGTH_LONG).show()
                                Log.d("LoginActivity", "Perfil guardado en Firestore. Redirigiendo a WelcomeActivity...") // Log
                                val intent = Intent(this, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Registro exitoso, pero fallo al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("LoginActivity", "Fallo al guardar perfil en Firestore. Redirigiendo a WelcomeActivity...", e) // Log de error
                                val intent = Intent(this, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        Log.e("LoginActivity", "Registro de Auth exitoso, pero UID es nulo.", task.exception) // Log de error
                        Toast.makeText(this, "Registro exitoso, pero UID de usuario no encontrado.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error de registro desconocido."
                    Toast.makeText(this, "Fallo en el registro: $errorMessage", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Fallo en el registro de Auth: $errorMessage", task.exception) // Log de error
                }
            }
    }
}