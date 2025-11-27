package com.example.gym4house

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var selectedImageUri: Uri? = null // Aquí guardamos la foto si eligen una

    // Lanzador de la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Mostrar la foto seleccionada en el círculo
            binding.ivProfilePic.setImageURI(uri)
            binding.ivProfilePic.setPadding(0,0,0,0) // Quitar padding para que llene el círculo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CLIC EN LA FOTO -> ABRIR GALERÍA
        binding.ivProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.textViewGoToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.buttonSiguiente.setOnClickListener {
            validarYPasar()
        }
    }

    private fun validarYPasar() {
        val nombre = binding.editTextNombre.text.toString().trim()
        val apellido = binding.editTextApellido.text.toString().trim()
        val email = binding.editTextCorreo.text.toString().trim()
        val password = binding.editTextContrasena.text.toString().trim()
        val confirm = binding.editTextConfirmarContrasena.text.toString().trim()

        if (nombre.isEmpty()) { binding.tilNombre.error = "Requerido"; return }
        if (apellido.isEmpty()) { binding.tilApellido.error = "Requerido"; return }
        if (email.isEmpty()) { binding.tilCorreo.error = "Requerido"; return }
        if (password.length < 6) { binding.tilContrasena.error = "Mínimo 6 caracteres"; return }
        if (password != confirm) { binding.tilConfirmarContrasena.error = "No coinciden"; return }

        // PASAR TODO AL PASO 2
        val intent = Intent(this, RegisterStep2Activity::class.java)
        intent.putExtra("nombre", nombre)
        intent.putExtra("apellido", apellido)
        intent.putExtra("email", email)
        intent.putExtra("password", password)

        // Pasamos la URI de la imagen como texto (si existe)
        if (selectedImageUri != null) {
            intent.putExtra("imageUri", selectedImageUri.toString())
        }

        startActivity(intent)
    }
}