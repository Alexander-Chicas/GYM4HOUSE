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
import com.google.firebase.firestore.ListenerRegistration
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

    private var progressListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fetchRoutines()
        observeProgress()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRoutines.layoutManager = LinearLayoutManager(context)
    }

    private fun fetchRoutines() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Gestionar visibilidad de la UI al empezar a cargar
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
                    binding.progressBar.isVisible = false // Ocultar progreso
                    if (routines.isNotEmpty()) {
                        binding.recyclerViewRoutines.isVisible = true
                        binding.recyclerViewRoutines.adapter = RoutineAdapter(routines) {
                            onRoutineSelected(it)
                        }
                    } else {
                        binding.noRoutinesMessage.isVisible = true // Mostrar mensaje de "vacío"
                    }
                }

            } catch (e: Exception) {
                Log.e("RoutineSelection", "Error fetching routines: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    binding.noRoutinesMessage.isVisible = true
                    Toast.makeText(context, "Error al cargar rutinas: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeProgress() {
        val userId = auth.currentUser?.uid ?: return

        progressListener = firestore.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("RoutineSelection", "Error listening progreso: ${e.message}", e)
                    return@addSnapshotListener
                }
            }
    }

    private fun onRoutineSelected(rutinaIncompleta: Rutina) {
        val userId = auth.currentUser?.uid
        if (userId == null || rutinaIncompleta.id.isEmpty()) {
            Toast.makeText(context, "Error: No se pudo iniciar la rutina.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.isVisible = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // CORRECCIÓN CLAVE AQUÍ: Obtener el documento completo de la rutina
                val rutinaDocument = firestore.collection("usuarios")
                    .document(userId)
                    .collection("rutinasGuardadas")
                    .document(rutinaIncompleta.id)
                    .get()
                    .await()

                // Deserializar directamente a la clase Rutina.
                // El método toObject() se encarga de convertir la lista de mapas a una lista de Ejercicio.
                val rutinaCompleta = rutinaDocument.toObject(Rutina::class.java)

                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    if (rutinaCompleta != null) {
                        (activity as? MainActivity)?.replaceFragment(WorkoutSessionFragment.newInstance(rutinaCompleta))
                    } else {
                        Toast.makeText(context, "Error al cargar la rutina completa.", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("RoutineSelection", "Error fetching routine for workout: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error al cargar los ejercicios: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressListener?.remove()
        _binding = null
    }
}