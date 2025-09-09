package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.FragmentHealthyRecipesBinding
import com.google.firebase.firestore.FirebaseFirestore

class HealthyRecipesFragment : Fragment() {

    private lateinit var binding: FragmentHealthyRecipesBinding
    private val db = FirebaseFirestore.getInstance()
    private val recipes = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthyRecipesBinding.inflate(inflater, container, false)

        adapter = RecipeAdapter(recipes) { recipe ->
            val fragment = RecipeDetailFragment.newInstance(recipe)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter

        loadRecipes()
        return binding.root
    }

    private fun loadRecipes() {
        db.collection("recipes").get()
            .addOnSuccessListener { result ->
                recipes.clear()
                for (doc in result) {
                    recipes.add(doc.toObject(Recipe::class.java))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Aquí podrías mostrar un Toast
            }
    }
}
