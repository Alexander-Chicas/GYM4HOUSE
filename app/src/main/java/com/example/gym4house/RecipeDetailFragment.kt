package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gym4house.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipe = it.getParcelable(ARG_RECIPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        recipe?.let { r ->
            // Textos Principales
            binding.tvRecipeDetailName.text = r.name
            binding.chipGoal.text = r.goal

            // CAMBIO AQUÍ: Mostramos el texto directo porque en tu BD es String
            // Si quieres darle formato de lista visualmente, puedes usar .replace()
            // Por ejemplo: r.ingredients.replace(",", "\n•") para saltos de línea
            binding.tvIngredientsList.text = r.ingredients

            binding.tvInstructions.text = r.instructions

            // Imagen Gigante
            Glide.with(this)
                .load(r.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.background_gym)
                .into(binding.ivRecipeDetailImage)

            // Macros (Evitamos crash si son null o 0 en la BD)
            setupMacroCard(binding.macroCalories.root, "${r.calories}", "Kcal")
            setupMacroCard(binding.macroProtein.root, "${r.protein}g", "Proteína")
            setupMacroCard(binding.macroCarbs.root, "${r.carbs}g", "Carbos")
        }
    }

    private fun setupMacroCard(view: View, value: String, label: String) {
        view.findViewById<TextView>(R.id.tvValue).text = value
        view.findViewById<TextView>(R.id.tvLabel).text = label
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe) =
            RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_RECIPE, recipe)
                }
            }
    }
}