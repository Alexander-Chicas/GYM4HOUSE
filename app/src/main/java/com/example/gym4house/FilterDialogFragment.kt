package com.example.gym4house

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.gym4house.databinding.DialogFilterRoutinesBinding
import com.google.android.material.chip.Chip

class FilterDialogFragment : DialogFragment() {

    private var _binding: DialogFilterRoutinesBinding? = null
    private val binding get() = _binding!!
    private var listener: FilterDialogListener? = null

    interface FilterDialogListener {
        fun onApplyFilters(tipo: String?, nivel: String?, duracionMax: Long?)
    }

    fun setFilterDialogListener(listener: FilterDialogListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        // Hacer el fondo del diálogo transparente para que se vean las esquinas redondeadas
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterRoutinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnApply.setOnClickListener {
            // Obtener Tipo seleccionado
            val selectedTypeId = binding.chipGroupType.checkedChipId
            val tipo = if (selectedTypeId != View.NO_ID) {
                binding.root.findViewById<Chip>(selectedTypeId).text.toString()
            } else null

            // Obtener Nivel seleccionado
            val selectedLevelId = binding.chipGroupLevel.checkedChipId
            val nivel = if (selectedLevelId != View.NO_ID) {
                binding.root.findViewById<Chip>(selectedLevelId).text.toString()
            } else null

            // Por ahora no usamos slider de duración en el diseño nuevo para simplificar,
            // pero podrías agregarlo. Enviamos null.
            listener?.onApplyFilters(tipo, nivel, null)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}