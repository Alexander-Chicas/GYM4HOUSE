package com.example.gym4house

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gym4house.Rutina // Asegúrate de que esta es la ruta correcta
import com.example.gym4house.databinding.ItemRoutineSelectionBinding

class RoutineAdapter(
    private val routines: List<Rutina>,
    private val onRoutineClick: (Rutina) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    inner class RoutineViewHolder(private val binding: ItemRoutineSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rutina: Rutina) {
            binding.tvRoutineName.text = rutina.nombreRutina
            binding.tvRoutineDescription.text = rutina.descripcion
            binding.tvRoutineLevel.text = rutina.nivel
            binding.tvRoutineDuration.text = "${rutina.duracionMinutos} min"

            binding.root.setOnClickListener {
                onRoutineClick(rutina)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(routines[position])
    }

    override fun getItemCount(): Int = routines.size
}