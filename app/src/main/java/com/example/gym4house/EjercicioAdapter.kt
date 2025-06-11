package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EjercicioAdapter(private val ejercicios: List<Ejercicio>) :
    RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder>() {

    // Clase interna para mantener las referencias de las vistas de cada elemento de la lista
    class EjercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreEjercicio: TextView = itemView.findViewById(R.id.textViewNombreEjercicio)
        val series: TextView = itemView.findViewById(R.id.textViewSeries)
        val repeticiones: TextView = itemView.findViewById(R.id.textViewRepeticiones)
        val descanso: TextView = itemView.findViewById(R.id.textViewDescanso)
    }

    // Se llama cuando el RecyclerView necesita un nuevo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio, parent, false)
        return EjercicioViewHolder(view)
    }

    // Se llama para mostrar los datos en una posición específica
    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.nombreEjercicio.text = ejercicio.nombreEjercicio
        holder.series.text = "Series: ${ejercicio.series}"
        holder.repeticiones.text = "Repeticiones: ${ejercicio.repeticiones}"
        holder.descanso.text = "Descanso: ${ejercicio.descansoSegundos} segundos"
    }

    // Retorna el número total de elementos en la lista
    override fun getItemCount(): Int {
        return ejercicios.size
    }
}