package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

class FilterDialogFragment : DialogFragment() {

    // Interfaz para comunicar los filtros seleccionados de vuelta al fragmento que lo llama
    interface FilterDialogListener {
        fun onApplyFilters(tipo: String?, nivel: String?, duracionMax: Long?)
    }

    private var listener: FilterDialogListener? = null

    // Método para adjuntar el listener desde el fragmento padre
    fun setFilterDialogListener(listener: FilterDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout del diálogo
        return inflater.inflate(R.layout.dialog_filter_routines, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerTipo: Spinner = view.findViewById(R.id.spinnerTipo)
        val spinnerNivel: Spinner = view.findViewById(R.id.spinnerNivel)
        val editTextDuracionMax: EditText = view.findViewById(R.id.editTextDuracionMax)
        val buttonCancelar: Button = view.findViewById(R.id.buttonCancelarFiltro)
        val buttonAplicar: Button = view.findViewById(R.id.buttonAplicarFiltro)

        // Configurar los Spinners con los arrays de strings
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.tipos_rutina_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTipo.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                it,
                R.array.niveles_rutina_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNivel.adapter = adapter
            }
        }


        // Listener para el botón Cancelar
        buttonCancelar.setOnClickListener {
            dismiss() // Cierra el diálogo
        }

        // Listener para el botón Aplicar Filtro
        buttonAplicar.setOnClickListener {
            val tipoSeleccionado = if (spinnerTipo.selectedItemPosition == 0) null else spinnerTipo.selectedItem.toString()
            val nivelSeleccionado = if (spinnerNivel.selectedItemPosition == 0) null else spinnerNivel.selectedItem.toString()
            val duracionMaxText = editTextDuracionMax.text.toString()
            val duracionMax: Long? = if (duracionMaxText.isNotBlank()) duracionMaxText.toLongOrNull() else null

            // Llamar al listener para pasar los filtros al fragmento padre
            listener?.onApplyFilters(tipoSeleccionado, nivelSeleccionado, duracionMax)
            dismiss() // Cierra el diálogo
        }
    }
}