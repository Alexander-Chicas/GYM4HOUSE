package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gym4house.databinding.ActivityEquipmentBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EquipmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentMode: String? = null

    companion object {
        const val LAUNCH_MODE_EXTRA = "launch_mode"
        const val MODE_REGISTER = "register"
        const val MODE_EDIT = "edit_profile" // Ajustado para coincidir con tu otro archivo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Diseño Dark Glass
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding = ActivityEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Detectar modo
        currentMode = intent.getStringExtra(LAUNCH_MODE_EXTRA)
        if (currentMode == MODE_EDIT) {
            binding.btnFinalizar.text = "Guardar Equipamiento"
            // Aquí podrías cargar el equipo existente si quisieras
        }

        // Configurar Chip "Ninguno" exclusivo
        binding.chipNinguno.setOnClickListener {
            binding.chipGroupEquipo.clearCheck()
            binding.chipNinguno.isChecked = true
        }

        // Si tocan otro, borrar "Ninguno"
        binding.chipGroupEquipo.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.contains(binding.chipNinguno.id) && checkedIds.size > 1) {
                val chipNone = findViewById<Chip>(binding.chipNinguno.id)
                if (chipNone.isChecked) chipNone.isChecked = false
            }
        }

        binding.btnFinalizar.setOnClickListener {
            guardarEquipamiento()
        }
    }

    private fun guardarEquipamiento() {
        val listaEquipo = mutableListOf<String>()
        val ids = binding.chipGroupEquipo.checkedChipIds

        for (id in ids) {
            val chip = binding.chipGroupEquipo.findViewById<Chip>(id)
            if (chip != null) listaEquipo.add(chip.text.toString())
        }

        if (listaEquipo.isEmpty()) {
            Toast.makeText(this, "Selecciona algo (o 'Ninguno')", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            binding.btnFinalizar.isEnabled = false
            binding.btnFinalizar.text = "Guardando..."

            // Guardamos como una lista simple de Strings en el documento del usuario
            db.collection("usuarios").document(uid)
                .update("equipamiento", listaEquipo)
                .addOnSuccessListener {

                    if (currentMode == MODE_REGISTER) {
                        // FIN DEL REGISTRO -> IR AL HOME
                        Toast.makeText(this, "¡Perfil Listo! A entrenar.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // MODO EDICIÓN -> VOLVER AL PERFIL
                        Toast.makeText(this, "Equipamiento actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener {
                    binding.btnFinalizar.isEnabled = true
                    binding.btnFinalizar.text = "Reintentar"
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}