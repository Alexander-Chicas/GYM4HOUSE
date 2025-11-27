package com.example.gym4house

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gym4house.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.textRecipeName.text = recipe.name
            binding.textRecipeGoal.text = recipe.goal

            // Colores Neón para el fondo del chip según el objetivo
            val (textColor, bgColor) = when(recipe.goal) {
                "Pérdida de Peso" -> Pair(Color.parseColor("#00E676"), Color.parseColor("#1A00E676")) // Verde
                "Ganancia Muscular" -> Pair(Color.parseColor("#2196F3"), Color.parseColor("#1A2196F3")) // Azul
                else -> Pair(Color.parseColor("#FF9800"), Color.parseColor("#1AFF9800")) // Naranja (Default)
            }

            binding.textRecipeGoal.setTextColor(textColor)
            binding.textRecipeGoal.setBackgroundColor(bgColor) // O usa backgroundTint si el drawable lo permite

            Glide.with(binding.root)
                .load(recipe.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.imageRecipe)

            binding.root.setOnClickListener { onClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size
}