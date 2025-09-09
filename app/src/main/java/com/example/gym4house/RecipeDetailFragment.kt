package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gym4house.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private lateinit var binding: FragmentRecipeDetailBinding
    private lateinit var recipe: Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipe = requireArguments().getSerializable(ARG_RECIPE) as Recipe
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

        binding.textRecipeTitle.text = recipe.name
        binding.textIngredients.text = recipe.ingredients
        binding.textSteps.text = recipe.steps
        binding.textMealTime.text = "Tiempo de comida: ${recipe.mealTime}"
        binding.textGoal.text = "Objetivo: ${recipe.goal}"

        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.imageRecipeDetail)

        return binding.root
    }

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_RECIPE, recipe)
            fragment.arguments = args
            return fragment
        }
    }
}
