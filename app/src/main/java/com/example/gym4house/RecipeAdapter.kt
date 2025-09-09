package com.example.gym4house

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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
            binding.textRecipeGoal.setTextColor(
                when(recipe.goal) {
                    "Pérdida de Peso" -> Color.parseColor("#4CAF50")
                    "Ganancia Muscular" -> Color.parseColor("#2196F3")
                    "Mantener Peso" -> Color.parseColor("#FF9800")
                    else -> Color.BLACK
                }
            )

            Glide.with(binding.root)
                .load(recipe.imageUrl)
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


