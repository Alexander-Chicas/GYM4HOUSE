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
        // Nuevos IDs del diseño moderno
        val tvNombre: TextView = itemView.findViewById(R.id.tvRoutineName)
        val tvFecha: TextView = itemView.findViewById(R.id.tvDate)
        val tvDuracion: TextView = itemView.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialRutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_rutina, parent, false)
        return HistorialRutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialRutinaViewHolder, position: Int) {
        val historial = historialList[position]

        holder.tvNombre.text = historial.nombreRutina

        // Formato de fecha corto y elegante (Ej: "26 Nov, 14:30")
        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val fechaStr = historial.fechaCompletado?.let {
            dateFormat.format(it.toDate())
        } ?: "Sin fecha"

        holder.tvFecha.text = fechaStr
        holder.tvDuracion.text = "${historial.duracionMinutos} min"
    }

    override fun getItemCount(): Int = historialList.size
}