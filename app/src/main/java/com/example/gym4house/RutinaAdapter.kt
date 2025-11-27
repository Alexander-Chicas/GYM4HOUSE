package com.example.gym4house

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RutinaAdapter(
    private val rutinasList: MutableList<Rutina>,
    // Usamos una lambda simple para manejar el click. Es más moderno y limpio que la interfaz antigua.
    private val onRoutineClick: (Rutina) -> Unit
) : RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder>() {

    inner class RutinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs actualizados según el archivo 'item_routine_card.xml'
        val tvNombre: TextView = itemView.findViewById(R.id.tvRoutineName)
        val tvNivel: TextView = itemView.findViewById(R.id.tvLevel)
        val tvDuracion: TextView = itemView.findViewById(R.id.tvDuration)
        // El botón ahora es un ImageButton (la flecha naranja)
        val btnStart: ImageButton = itemView.findViewById(R.id.btnStartRoutine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_card, parent, false)
        return RutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = rutinasList[position]

        // Asignar datos
        holder.tvNombre.text = rutina.nombreRutina

        // Manejo de valores vacíos para que no se vea feo
        holder.tvNivel.text = if (rutina.nivel.isNotEmpty()) rutina.nivel else "General"
        holder.tvDuracion.text = "${rutina.duracionMinutos} min"

        // Configurar el click.
        // Hacemos que tanto tocar la tarjeta como el botón de flecha funcionen.
        val listener = View.OnClickListener {
            onRoutineClick(rutina)
        }

        holder.itemView.setOnClickListener(listener)
        holder.btnStart.setOnClickListener(listener)
    }

    override fun getItemCount(): Int = rutinasList.size

    fun updateList(newList: List<Rutina>) {
        rutinasList.clear()
        rutinasList.addAll(newList)
        notifyDataSetChanged()
    }
}