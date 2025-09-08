package com.example.gym4house

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.gym4house.databinding.FragmentRemindersSettingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemindersSettingsFragment : Fragment() {

    private var _binding: FragmentRemindersSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedHour: Int = 9
    private var selectedMinute: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup spinner for reminder frequency
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_frequency_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerReminderFrequency.adapter = adapter
        }

        setupListeners()
        loadReminderSettings()
    }

    private fun setupListeners() {
        binding.switchEnableReminders.setOnCheckedChangeListener { _, isChecked ->
            Log.d("RemindersSettings", "Switch 'Habilitar Recordatorios' cambiado a: $isChecked")
            toggleReminderControls(isChecked)
            // Si el switch se desactiva, guarda el estado inmediatamente para deshabilitar recordatorios.
            if (!isChecked) {
                saveReminderSettings(disableOnly = true)
            }
        }

        binding.buttonSelectTime.setOnClickListener {
            Log.d("RemindersSettings", "Botón 'Seleccionar Hora' clicado.")
            showTimePicker()
        }

        binding.buttonSaveReminders.setOnClickListener {
            Log.d("RemindersSettings", "Botón 'Guardar Ajustes' clicado.")
            saveReminderSettings()
        }
    }

    private fun showTimePicker() {
        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // O CLOCK_24H
            .setHour(selectedHour)
            .setMinute(selectedMinute)
            .setTitleText("Seleccionar hora del recordatorio")
            .build()

        materialTimePicker.addOnPositiveButtonClickListener {
            selectedHour = materialTimePicker.hour
            selectedMinute = materialTimePicker.minute
            updateTimeButtonText()
            Log.d("RemindersSettings", "Hora seleccionada: ${selectedHour}:${selectedMinute}")
        }
        materialTimePicker.show(parentFragmentManager, "TIME_PICKER_TAG")
    }

    private fun updateTimeButtonText() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Formato 12 horas con AM/PM
        binding.buttonSelectTime.text = sdf.format(calendar.time)
    }

    private fun toggleReminderControls(enabled: Boolean) {
        binding.spinnerReminderFrequency.isEnabled = enabled
        binding.buttonSelectTime.isEnabled = enabled
        // El botón de guardar siempre debería estar habilitado si los recordatorios están habilitados,
        // o si los deshabilitaste y quieres guardar ese cambio.
        binding.buttonSaveReminders.isEnabled = true 
    }

    private fun loadReminderSettings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("RemindersSettings", "Usuario no autenticado, no se pueden cargar los ajustes de recordatorio.")
            toggleReminderControls(false) // Deshabilita los controles si no hay usuario
            return
        }

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("RemindersSettings", "Carga de ajustes de recordatorio exitosa. Documento existe: ${document.exists()}")
                if (document.exists()) {
                    val reminderSettings = document.get("reminderSettings") as? Map<String, Any>
                    reminderSettings?.let {
                        val enabled = it["enabled"] as? Boolean ?: false
                        val frequency = it["frequency"] as? String
                        val hour = (it["hour"] as? Long)?.toInt() ?: 9
                        val minute = (it["minute"] as? Long)?.toInt() ?: 0

                        binding.switchEnableReminders.isChecked = enabled
                        selectedHour = hour
                        selectedMinute = minute

                        // Establecer la frecuencia en el Spinner
                        if (frequency != null) {
                            val adapter = binding.spinnerReminderFrequency.adapter as? ArrayAdapter<String>
                            val spinnerPosition = adapter?.getPosition(frequency) ?: -1
                            if (spinnerPosition != -1) {
                                binding.spinnerReminderFrequency.setSelection(spinnerPosition)
                            }
                        }
                        updateTimeButtonText()
                        toggleReminderControls(enabled) // Asegura que los controles se habiliten/deshabiliten según el estado cargado
                        Log.d("RemindersSettings", "Ajustes cargados: Habilitado=$enabled, Frecuencia=$frequency, Hora=${hour}:${minute}")
                    } ?: run {
                        Log.d("RemindersSettings", "No se encontraron ajustes de recordatorio. Aplicando por defecto.")
                        binding.switchEnableReminders.isChecked = false
                        toggleReminderControls(false)
                        updateTimeButtonText() // Asegura que el botón de hora muestre la hora por defecto
                    }
                } else {
                    Log.d("RemindersSettings", "Documento de usuario no existe al cargar ajustes de recordatorio.")
                    binding.switchEnableReminders.isChecked = false
                    toggleReminderControls(false)
                    updateTimeButtonText()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RemindersSettings", "Error al cargar ajustes de recordatorio: ${e.message}", e)
                Snackbar.make(binding.root, "Error al cargar recordatorios: ${e.message}", Snackbar.LENGTH_LONG).show()
                binding.switchEnableReminders.isChecked = false
                toggleReminderControls(false)
                updateTimeButtonText()
            }
    }

    private fun saveReminderSettings(disableOnly: Boolean = false) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Snackbar.make(binding.root, "Usuario no autenticado. No se pueden guardar los ajustes.", Snackbar.LENGTH_SHORT).show()
            Log.e("RemindersSettings", "Usuario no autenticado, abortando guardado.")
            return
        }

        val enabled = if (disableOnly) false else binding.switchEnableReminders.isChecked
        val frequency = if (enabled) binding.spinnerReminderFrequency.selectedItem?.toString() else null
        val hourToSave = if (enabled) selectedHour else null
        val minuteToSave = if (enabled) selectedMinute else null

        val reminderSettings = hashMapOf(
            "enabled" to enabled,
            "frequency" to frequency,
            "hour" to hourToSave,
            "minute" to minuteToSave
        )

        Log.d("RemindersSettings", "Guardando ajustes: $reminderSettings")

        firestore.collection("usuarios").document(userId)
            .update("reminderSettings", reminderSettings)
            .addOnSuccessListener {
                if (disableOnly) {
                    Snackbar.make(binding.root, "Recordatorios deshabilitados.", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Ajustes de recordatorio guardados correctamente.", Snackbar.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Volver al fragmento anterior
                }
                Log.d("RemindersSettings", "Ajustes de recordatorio guardados exitosamente.")
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Error al guardar recordatorios: ${e.message}", Snackbar.LENGTH_LONG).show()
                Log.e("RemindersSettings", "Error al guardar ajustes de recordatorio: ${e.message}", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}