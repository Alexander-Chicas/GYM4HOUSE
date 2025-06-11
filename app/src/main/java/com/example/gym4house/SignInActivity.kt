package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
// No necesitas FirebaseFirestore en SignInActivity a menos que guardes/leas algo específico aquí
// import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    // private lateinit var db: FirebaseFirestore // Elimina esta línea si no la usas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
        // db = FirebaseFirestore.getInstance() // Elimina o comenta esta línea si no la usas

        // Listener para el botón de inicio de sesión
        binding.buttonSignIn.setOnClickListener {
            performLogin()
        }

        // Listener para el TextView que lleva a la pantalla de Registro
        binding.textViewGoToRegister.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // No uses finish() aquí si quieres que el usuario pueda volver a la pantalla de inicio de sesión
        }
    }

    // *** ¡ESTE MÉTODO onStart() FUE ELIMINADO/COMENTADO! ***
    /*
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    */

    private fun performLogin() {
        val email = binding.editTextSignInCorreo.text.toString().trim()
        val password = binding.editTextSignInContrasena.text.toString().trim()

        // 1. Validaciones básicas de campos
        if (email.isEmpty()) {
            binding.editTextSignInCorreo.error = "El correo no puede estar vacío"
            binding.editTextSignInCorreo.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextSignInCorreo.error = "Ingresa un correo electrónico válido"
            binding.editTextSignInCorreo.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.editTextSignInContrasena.error = "La contraseña no puede estar vacía"
            binding.editTextSignInContrasena.requestFocus()
            return
        }

        // 2. Iniciar sesión con Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                    // Redirige a la MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Finaliza esta actividad
                } else {
                    // Fallo en el inicio de sesión
                    val errorMessage = task.exception?.message ?: "Error al iniciar sesión."
                    Toast.makeText(this, "Fallo al iniciar sesión: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}