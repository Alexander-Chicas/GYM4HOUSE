package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout para esta actividad a 'activity_welcome.xml'
        setContentView(R.layout.activity_welcome)

        // Referenciar los botones de la interfaz de usuario por sus IDs
        val buttonCreateAccount = findViewById<Button>(R.id.buttonCreateAccount)
        val buttonSignIn = findViewById<Button>(R.id.buttonSignIn)

        // Configurar el "listener" para el clic del botón "Crear Cuenta"
        buttonCreateAccount.setOnClickListener {
            // Crear un Intent para navegar a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent) // Iniciar LoginActivity
            // No llamamos a finish() aquí porque el usuario podría querer volver a la pantalla de bienvenida.
        }

        // Configurar el "listener" para el clic del botón "Iniciar Sesión"
        buttonSignIn.setOnClickListener {
            // Por ahora, vamos a navegar a MainActivity como un placeholder para la pantalla de inicio de sesión.
            // Más adelante, crearemos una 'SignInActivity' dedicada.
            val intent = Intent(this, MainActivity::class.java) // Placeholder: cambiar a SignInActivity
            startActivity(intent) // Iniciar la actividad de inicio de sesión (temporalmente MainActivity)
            // Aquí podrías decidir si quieres llamar a finish() o no.
            // Si la pantalla de inicio de sesión no permite volver a la bienvenida, usa finish().
            // Por ahora, dejémoslo sin finish() para permitir el back.
        }
    }
}