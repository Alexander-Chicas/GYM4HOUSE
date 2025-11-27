package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager // Importante para quitar la barra
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INICIO DEL TRUCO DE DISEÑO ---
        // Estas líneas eliminan los límites de la pantalla (la barra de estado y navegación)
        // haciendo que tu foto oscura cubra TODO el celular. ¡Adiós barra café!
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // --- FIN DEL TRUCO ---

        // Establece el layout para esta actividad
        setContentView(R.layout.activity_welcome)

        // Referenciar los botones
        val buttonCreateAccount = findViewById<Button>(R.id.buttonCreateAccount)
        val buttonSignIn = findViewById<Button>(R.id.buttonSignIn)
        // Nota: Si quieres darle funcionalidad al botón de Google más tarde,
        // agrégalo aquí: val buttonGoogle = findViewById<Button>(R.id.buttonSignInGmail)

        // Configurar el click del botón "Crear Cuenta"
        buttonCreateAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Configurar el click del botón "Iniciar Sesión"
        buttonSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}