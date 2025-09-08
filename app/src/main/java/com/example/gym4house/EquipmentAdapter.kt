package com.example.gym4house

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gym4house.databinding.ItemEquipmentBinding

class EquipmentAdapter(
    private val equipmentList: MutableList<Equipment>,
    private val onItemCheckChanged: (Int, Boolean) -> Unit,
    private val onItemDelete: (Equipment) -> Unit
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    class EquipmentViewHolder(val binding: ItemEquipmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = ItemEquipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val currentEquipment = equipmentList[position]
        holder.binding.tvEquipmentName.text = currentEquipment.name
        holder.binding.cbEquipmentCheck.isChecked = currentEquipment.isSelected
        holder.binding.btnDelete.setOnClickListener {
            onItemDelete(currentEquipment)
        }

        // Listener para el CheckBox
        holder.binding.cbEquipmentCheck.setOnCheckedChangeListener { _, isChecked ->
            currentEquipment.isSelected = isChecked
            onItemCheckChanged(position, isChecked)
            Log.d("EquipmentAdapter", "Item '${currentEquipment.name}' isSelected: $isChecked")
        }
    }

    override fun getItemCount() = equipmentList.size

    fun setItems(newItems: List<Equipment>) {
        equipmentList.clear()
        equipmentList.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getEquipmentList(): List<Equipment> = equipmentList

    fun addItem(equipment: Equipment) {
        equipmentList.add(equipment)
        notifyItemInserted(equipmentList.size - 1)
    }

    fun removeItem(equipment: Equipment) {
        val index = equipmentList.indexOf(equipment)
        if (index != -1) {
            equipmentList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}