package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Importar Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RutinaAdapter(
    private val rutinasList: MutableList<Rutina>,
    private val listener: OnRoutineActionListener // <-- ¡NUEVO! Pasa un listener al constructor
) : RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder>() {

    // Interfaz para definir las acciones que el adaptador puede comunicar
    interface OnRoutineActionListener {
        fun onSaveRoutineClick(rutina: Rutina)
        // Puedes añadir más acciones aquí, como onRoutineClick(rutina: Rutina)
    }

    class RutinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreRutina: TextView = itemView.findViewById(R.id.textViewCardNombreRutina)
        val descripcion: TextView = itemView.findViewById(R.id.textViewCardDescripcionRutina)
        val tipo: TextView = itemView.findViewById(R.id.textViewCardTipo)
        val nivel: TextView = itemView.findViewById(R.id.textViewCardNivel)
        val duracion: TextView = itemView.findViewById(R.id.textViewCardDuracion)
        val buttonGuardar: Button = itemView.findViewById(R.id.buttonGuardarRutina) // <-- ¡NUEVO!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_card, parent, false)
        return RutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = rutinasList[position]
        holder.nombreRutina.text = rutina.nombreRutina
        holder.descripcion.text = rutina.descripcion
        holder.tipo.text = "Tipo: ${rutina.tipo}"
        holder.nivel.text = "Nivel: ${rutina.nivel}"
        holder.duracion.text = "Duración: ${rutina.duracionMinutos} min"

        // Configurar el click listener para el botón Guardar
        holder.buttonGuardar.setOnClickListener {
            listener.onSaveRoutineClick(rutina) // Llama al método del listener con la rutina actual
        }
    }

    override fun getItemCount(): Int {
        return rutinasList.size
    }

    fun updateList(newList: List<Rutina>) {
        rutinasList.clear()
        rutinasList.addAll(newList)
        notifyDataSetChanged()
    }
}