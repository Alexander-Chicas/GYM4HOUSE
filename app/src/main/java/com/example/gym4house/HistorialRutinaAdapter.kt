package com.example.gym4house

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialRutinaAdapter(private val historialList: List<HistorialRutina>) :
    RecyclerView.Adapter<HistorialRutinaAdapter.HistorialRutinaViewHolder>() {

    class HistorialRutinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreRutina: TextView = itemView.findViewById(R.id.textViewHistorialNombreRutina)
        val fechaCompletado: TextView = itemView.findViewById(R.id.textViewHistorialFecha)
        val duracion: TextView = itemView.findViewById(R.id.textViewHistorialDuracion)
        val nivel: TextView = itemView.findViewById(R.id.textViewHistorialNivel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialRutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_rutina, parent, false)
        return HistorialRutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialRutinaViewHolder, position: Int) {
        val historial = historialList[position]
        holder.nombreRutina.text = historial.nombreRutina

        // Formatear la fecha para mostrarla de forma legible
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.fechaCompletado.text = "Fecha: ${dateFormat.format(historial.fechaCompletado.toDate())}"

        holder.duracion.text = "Duración: ${historial.duracionMinutos} min"
        holder.nivel.text = "Nivel: ${historial.nivel}"
    }

    override fun getItemCount(): Int {
        return historialList.size
    }
}