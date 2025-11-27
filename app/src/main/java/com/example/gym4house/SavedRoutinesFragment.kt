package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.FragmentSavedRoutinesBinding // ViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedRoutinesFragment : Fragment() {

    private var _binding: FragmentSavedRoutinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var savedRoutinesAdapter: RutinaAdapter
    private val savedRoutinesList = mutableListOf<Rutina>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedRoutinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadSavedRoutines()
    }

    private fun setupRecyclerView() {
        // Configuramos el adaptador con la Lambda moderna.
        // Al hacer clic en una rutina guardada, la iniciamos directamente.
        savedRoutinesAdapter = RutinaAdapter(savedRoutinesList) { rutinaSeleccionada ->
            startWorkoutSession(rutinaSeleccionada)
        }

        binding.recyclerViewSavedRoutines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = savedRoutinesAdapter
        }
    }

    private fun loadSavedRoutines() {
        val userId = auth.currentUser?.uid ?: run {
            showEmptyState(true)
            return
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("rutinasGuardadas")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = documents.mapNotNull { doc ->
                    doc.toObject(Rutina::class.java).apply { id = doc.id }
                }

                savedRoutinesAdapter.updateList(fetchedRoutines)
                showEmptyState(fetchedRoutines.isEmpty())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error cargando guardadas", Toast.LENGTH_SHORT).show()
                showEmptyState(true)
            }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (_binding == null) return
        if (isEmpty) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewSavedRoutines.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewSavedRoutines.visibility = View.VISIBLE
        }
    }

    private fun startWorkoutSession(rutina: Rutina) {
        // Usamos el método replaceFragment de MainActivity para iniciar la sesión
        // Esto nos lleva a la pantalla de "Entrenamiento"
        (activity as? MainActivity)?.replaceFragment(WorkoutSessionFragment.newInstance(rutina))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}