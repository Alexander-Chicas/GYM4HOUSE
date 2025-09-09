package com.example.gym4house

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.FragmentRoutineSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RoutineSelectionFragment : Fragment() {

    private var _binding: FragmentRoutineSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.recyclerViewRoutines.layoutManager = LinearLayoutManager(context)
        fetchRoutines()
    }

    private fun fetchRoutines() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.isVisible = true
        binding.noRoutinesMessage.isVisible = false
        binding.recyclerViewRoutines.isVisible = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firestore.collection("usuarios")
                    .document(userId)
                    .collection("rutinasGuardadas")
                    .get()
                    .await()

                val routines = result.documents.mapNotNull { doc ->
                    doc.toObject(Rutina::class.java)?.apply { id = doc.id }
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    if (routines.isNotEmpty()) {
                        binding.recyclerViewRoutines.isVisible = true
                        binding.recyclerViewRoutines.adapter = RoutineAdapter(routines) { rutina ->
                            onRoutineSelected(rutina)
                        }
                    } else {
                        binding.noRoutinesMessage.isVisible = true
                    }
                }
            } catch (e: Exception) {
                Log.e("RoutineSelection", "Error fetching routines: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    binding.noRoutinesMessage.isVisible = true
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onRoutineSelected(rutina: Rutina) {
        (activity as? MainActivity)?.replaceFragment(
            WorkoutSessionFragment.newInstance(rutina)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
