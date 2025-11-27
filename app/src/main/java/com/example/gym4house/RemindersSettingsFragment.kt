package com.example.gym4house

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentRemindersSettingsBinding
import java.util.Calendar
import java.util.Locale

class RemindersSettingsFragment : Fragment() {

    private var _binding: FragmentRemindersSettingsBinding? = null
    private val binding get() = _binding!!

    // Variables para guardar la hora seleccionada (por defecto 08:00)
    private var selectedHour = 8
    private var selectedMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar preferencias guardadas (si existen)
        loadSavedPreferences()
        updateTimeUI()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSaveReminder.setOnClickListener {
            saveReminderSettings()
        }
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateTimeUI()
            },
            selectedHour,
            selectedMinute,
            false // false para formato 12h (AM/PM), true para 24h
        )
        timePicker.show()
    }

    private fun updateTimeUI() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)

        // Formato bonito (ej: 07:30 PM)
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.tvSelectedTime.text = timeFormat.format(calendar.time)
    }

    private fun saveReminderSettings() {
        val isEnabled = binding.switchReminder.isChecked

        // Aquí iría tu lógica para programar la alarma real con AlarmManager
        // Por ahora simulamos el guardado

        val context = requireContext()
        val prefs = context.getSharedPreferences("gym_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("reminder_hour", selectedHour)
            putInt("reminder_minute", selectedMinute)
            putBoolean("reminder_enabled", isEnabled)
            apply()
        }

        if (isEnabled) {
            // ReminderScheduler.scheduleReminder(context, selectedHour, selectedMinute)
            Toast.makeText(context, "¡Recordatorio activado!", Toast.LENGTH_SHORT).show()
        } else {
            // ReminderScheduler.cancelReminder(context)
            Toast.makeText(context, "Recordatorios desactivados", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.popBackStack()
    }

    private fun loadSavedPreferences() {
        val prefs = requireContext().getSharedPreferences("gym_prefs", android.content.Context.MODE_PRIVATE)
        selectedHour = prefs.getInt("reminder_hour", 8)
        selectedMinute = prefs.getInt("reminder_minute", 0)
        binding.switchReminder.isChecked = prefs.getBoolean("reminder_enabled", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}