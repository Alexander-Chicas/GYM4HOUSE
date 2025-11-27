package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityHealthRestrictionsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HealthRestrictionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthRestrictionsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Variable para detectar si venimos de Registro o de Editar Perfil
    private var currentMode: String? = null

    companion object {
        const val LAUNCH_MODE_EXTRA = "launch_mode"
        const val MODE_REGISTER = "mode_register"
        const val MODE_EDIT = "mode_edit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Quitar barra de estado
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding = ActivityHealthRestrictionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Leer el modo de lanzamiento (¿Registro o Edición?)
        currentMode = intent.getStringExtra(LAUNCH_MODE_EXTRA)
        if (currentMode == MODE_EDIT) {
            binding.btnGuardar.text = "Guardar Cambios"
            // Aquí podrías cargar los datos existentes si quisieras
        }

        // Configurar lógica de los chips "Ninguna"
        setupExclusiveNone(binding.cgAlergias, binding.chipNingunaAlergia.id)
        setupExclusiveNone(binding.cgCondiciones, binding.chipNingunaCondicion.id)
        setupExclusiveNone(binding.cgFisico, binding.chipNingunaFisico.id)

        binding.btnGuardar.setOnClickListener {
            guardarDatos()
        }
    }

    // Función para que al marcar "Ninguna", se desmarquen los demás
    private fun setupExclusiveNone(chipGroup: ChipGroup, noneId: Int) {
        chipGroup.findViewById<Chip>(noneId).setOnClickListener {
            // Limpiar todo y dejar solo este marcado
            chipGroup.clearCheck()
            chipGroup.check(noneId)
        }

        // Si tocan cualquier otro chip, desmarcar "Ninguna"
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.contains(noneId) && checkedIds.size > 1) {
                // Si hay varios marcados y uno es "Ninguna", quitar "Ninguna"
                val chipNone = group.findViewById<Chip>(noneId)
                if (chipNone.isChecked) chipNone.isChecked = false
            }
        }
    }

    private fun guardarDatos() {
        val alergias = obtenerSeleccion(binding.cgAlergias)
        val condiciones = obtenerSeleccion(binding.cgCondiciones)
        val fisico = obtenerSeleccion(binding.cgFisico)
        val notas = binding.editTextNotas.text.toString().trim()

        val mapaSalud = hashMapOf(
            "alergias" to alergias,
            "condiciones" to condiciones,
            "lesiones" to fisico,
            "notas" to notas
        )

        val uid = auth.currentUser?.uid
        if (uid != null) {
            binding.btnGuardar.isEnabled = false
            binding.btnGuardar.text = "Guardando..."

            db.collection("usuarios").document(uid)
                .update("salud", mapaSalud)
                .addOnSuccessListener {
                    Toast.makeText(this, "Información de salud guardada", Toast.LENGTH_SHORT).show()

                    // --- NAVEGACIÓN INTELIGENTE ---
                    if (currentMode == MODE_REGISTER) {
                        // Si es registro, vamos a la siguiente pantalla: Equipamiento
                        val intent = Intent(this, EquipmentActivity::class.java)
                        intent.putExtra(EquipmentActivity.LAUNCH_MODE_EXTRA, EquipmentActivity.MODE_REGISTER)
                        startActivity(intent)
                        finish()
                    } else {
                        // Si es edición, solo cerramos para volver al perfil
                        finish()
                    }
                }
                .addOnFailureListener {
                    binding.btnGuardar.isEnabled = true
                    binding.btnGuardar.text = if (currentMode == MODE_EDIT) "Guardar Cambios" else "Guardar y Continuar"
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerSeleccion(group: ChipGroup): List<String> {
        val lista = mutableListOf<String>()
        for (id in group.checkedChipIds) {
            val chip = group.findViewById<Chip>(id)
            if (chip != null) lista.add(chip.text.toString())
        }
        if (lista.isEmpty()) return listOf("No especificado")
        return lista
    }
}