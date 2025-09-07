package com.example.gym4house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.gym4house.databinding.FragmentRemindersSettingsBinding

class RemindersSettingsFragment : Fragment() {

    private var _binding: FragmentRemindersSettingsBinding? = null
    private val binding get() = _binding!!

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

        // Setup spinner for reminder frequency
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_frequency_options, // You'll need to define this in arrays.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerReminderFrequency.adapter = adapter
        }

        binding.buttonSelectTime.setOnClickListener {
            // Implement time picker dialog
            Toast.makeText(requireContext(), "Seleccionar hora", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSaveReminders.setOnClickListener {
            // Implement saving reminders logic
            Toast.makeText(requireContext(), "Ajustes de recordatorio guardados", Toast.LENGTH_SHORT).show()
            // Optionally, pop back to the previous fragment
            // parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
