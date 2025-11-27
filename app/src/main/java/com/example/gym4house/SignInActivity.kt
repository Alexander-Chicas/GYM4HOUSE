package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager // <--- Necesario para el truco visual
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. TRUCO VISUAL: Quitar la barra de estado (Status Bar)
        // Esto es vital para que tu fondo oscuro se vea completo
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Listener botón volver (Si pusiste la flecha atrás en el XML)
        binding.btnBack.setOnClickListener {
            finish() // Vuelve a la pantalla de bienvenida
        }

        // Listener para el botón de inicio de sesión
        binding.buttonSignIn.setOnClickListener {
            performLogin()
        }

        // Listener para ir a Registro
        binding.textViewGoToRegister.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java) // Asumo que LoginActivity es tu registro
            startActivity(intent)
        }
    }

    private fun performLogin() {
        // Nota: Al usar TextInputLayout, es mejor obtener el texto así, pero como lo tienes funciona bien.
        val email = binding.editTextSignInCorreo.text.toString().trim()
        val password = binding.editTextSignInContrasena.text.toString().trim()

        // Validaciones
        if (email.isEmpty()) {
            // Un toque pro: Poner el error en el Layout contenedor, no en el edittext
            binding.tilEmail.error = "El correo es necesario"
            binding.editTextSignInCorreo.requestFocus()
            return
        } else {
            binding.tilEmail.error = null // Limpiar error
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Falta la contraseña"
            binding.editTextSignInContrasena.requestFocus()
            return
        } else {
            binding.tilPassword.error = null
        }

        // Login con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "¡A entrenar!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Error al iniciar sesión."
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}