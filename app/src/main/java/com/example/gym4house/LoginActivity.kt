package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gym4house.data.db.AppDatabase // Asegúrate de que esta importación sea correcta
import com.example.gym4house.data.entity.Usuario // Asegúrate de que esta importación sea correcta
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var appDatabase: AppDatabase // Instancia de la base de datos Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout para esta actividad a 'activity_login.xml'
        setContentView(R.layout.activity_login)

        // Inicializar la instancia de la base de datos Room.
        // Se hace aquí porque la actividad necesita el contexto para crear la base de datos.
        appDatabase = AppDatabase.getDatabase(this)

        // Referenciar los elementos de la interfaz de usuario (UI) por sus IDs
        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val editTextApellido = findViewById<EditText>(R.id.editTextApellido)
        val editTextCorreo = findViewById<EditText>(R.id.editTextCorreo)
        val editTextContrasena = findViewById<EditText>(R.id.editTextContrasena)
        val editTextConfirmarContrasena = findViewById<EditText>(R.id.editTextConfirmarContrasena)
        val editTextRol = findViewById<EditText>(R.id.editTextRol) // Por ahora, se usará como un simple campo de texto
        val buttonRegistrar = findViewById<Button>(R.id.buttonRegistrar)

        // Configurar el "listener" para el clic del botón de registro
        buttonRegistrar.setOnClickListener {
            // 1. Obtener los textos de los campos y limpiarlos de espacios en blanco
            val nombre = editTextNombre.text.toString().trim()
            val apellido = editTextApellido.text.toString().trim()
            val correo = editTextCorreo.text.toString().trim()
            val contrasena = editTextContrasena.text.toString().trim()
            val confirmarContrasena = editTextConfirmarContrasena.text.toString().trim()
            val rol = editTextRol.text.toString().trim()

            // 2. Realizar validaciones básicas
            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() ||
                contrasena.isEmpty() || confirmarContrasena.isEmpty() || rol.isEmpty()) {
                // Mostrar un mensaje si algún campo está vacío
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Salir de la función si la validación falla
            }

            if (contrasena != confirmarContrasena) {
                // Mostrar un mensaje si las contraseñas no coinciden
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Consideraciones importantes para una aplicación real (más allá del alcance actual) ---
            // - **Seguridad de Contraseña:** En una aplicación real, NUNCA guardarías la contraseña directamente (sin hashear).
            //   Deberías usar una función de hash segura (ej. BCrypt) antes de guardarla.
            // - **Validación de Correo:** Implementar una validación de formato de correo electrónico más robusta.
            // - **Selección de Rol:** En lugar de un EditText, usar un Spinner (menú desplegable) o RadioButtons para el rol.

            // 3. Iniciar una coroutine para realizar operaciones de base de datos en segundo plano
            // Las operaciones de Room (insert, query, etc.) que son 'suspend' deben llamarse desde una coroutine.
            // 'lifecycleScope.launch' es ideal porque maneja el ciclo de vida de la coroutine con el de la actividad.
            lifecycleScope.launch {
                try {
                    // 4. Verificar si el correo electrónico ya existe en la base de datos
                    val existingUser = appDatabase.usuarioDao().getUsuarioByEmail(correo)

                    if (existingUser != null) {
                        // Si el correo ya existe, notificar al usuario
                        Toast.makeText(this@LoginActivity, "El correo electrónico ya está registrado.", Toast.LENGTH_LONG).show()
                        Log.w("Registro", "Intento de registro con correo existente: $correo")
                    } else {
                        // 5. Crear un nuevo objeto Usuario con los datos del formulario
                        val nuevoUsuario = Usuario(
                            nombre = nombre,
                            apellido = apellido,
                            correoElectronico = correo,
                            contrasenaHash = contrasena, // ¡ATENCIÓN: Hashear en producción!
                            rol = rol,
                            fechaNacimiento = 0L // Valor temporal, se actualizará en la pantalla de perfil
                        )

                        // 6. Insertar el nuevo usuario en la base de datos
                        // El método insertUsuario devuelve el ID de la fila insertada.
                        val userId = appDatabase.usuarioDao().insertUsuario(nuevoUsuario)

                        if (userId > 0) { // Si userId es mayor que 0, la inserción fue exitosa
                            Toast.makeText(this@LoginActivity, "¡Registro exitoso! ID: $userId", Toast.LENGTH_LONG).show()
                            Log.d("Registro", "Usuario registrado: $nombre $apellido con ID: $userId")

                            // 7. Opcional: Limpiar los campos del formulario después del registro exitoso
                            editTextNombre.text.clear()
                            editTextApellido.text.clear()
                            editTextCorreo.text.clear()
                            editTextContrasena.text.clear()
                            editTextConfirmarContrasena.text.clear()
                            editTextRol.text.clear()

                            // 8. Navegar a la siguiente actividad (MainActivity en este caso)
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Finalizar LoginActivity para que el usuario no pueda volver atrás con el botón de retroceso
                        } else {
                            // Si userId no es > 0, algo salió mal con la inserción
                            Toast.makeText(this@LoginActivity, "Error al registrar usuario.", Toast.LENGTH_LONG).show()
                            Log.e("Registro", "La inserción del usuario devolvió un ID <= 0")
                        }
                    }
                } catch (e: Exception) {
                    // Capturar cualquier excepción que ocurra durante la operación de base de datos
                    Log.e("Registro", "Error inesperado al intentar registrar usuario: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Error interno al registrar. Intente de nuevo.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}