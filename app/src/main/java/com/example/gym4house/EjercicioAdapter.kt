package com.example.gym4house

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.RawValue

class EjercicioAdapter(private val ejercicios: List<Ejercicio>) :
    RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder>() {

    class EjercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreEjercicio: TextView = itemView.findViewById(R.id.textViewNombreEjercicio)
        val series: TextView = itemView.findViewById(R.id.textViewSeries)
        val repeticiones: TextView = itemView.findViewById(R.id.textViewRepeticiones)
        val descanso: TextView = itemView.findViewById(R.id.textViewDescanso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_workout, parent, false)
        return EjercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]

        // Ahora puedes acceder a las propiedades directamente desde el objeto 'Ejercicio'
        holder.nombreEjercicio.text = ejercicio.nombreEjercicio
        holder.series.text = "Series: ${ejercicio.series}"
        holder.repeticiones.text = "Repeticiones: ${ejercicio.repeticiones}"
        holder.descanso.text = "Descanso: ${ejercicio.descansoSegundos} segundos"
    }

    override fun getItemCount(): Int {
        return ejercicios.size
    }
}