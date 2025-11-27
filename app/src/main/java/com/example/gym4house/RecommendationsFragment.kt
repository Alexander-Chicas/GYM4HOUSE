package com.example.gym4house

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.FragmentRecommendationsBinding // Binding generado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecommendationsFragment : Fragment() {

    private var _binding: FragmentRecommendationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recommendationsAdapter: RutinaAdapter
    private val recommendedRoutinesList = mutableListOf<Rutina>()

    // Variables de perfil
    private var userLevel: String? = null
    private var preferredExerciseType: String? = null
    private var userEquipmentList: List<String>? = null
    private var healthRestrictions: Map<String, Any>? = null
    private var userActivityLevel: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
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
        fetchUserProfile()
    }

    private fun setupRecyclerView() {
        // Usamos el adaptador moderno con lambda
        recommendationsAdapter = RutinaAdapter(recommendedRoutinesList) { rutinaSeleccionada ->
            saveRoutine(rutinaSeleccionada)
        }

        binding.recyclerViewRecommendations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recommendationsAdapter
        }
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Inicia sesión para ver recomendaciones", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userLevel = document.getString("nivelExperiencia")
                    preferredExerciseType = document.getString("tipo_ejercicio_preferido")
                    healthRestrictions = document.get("restriccionesSalud") as? Map<String, Any>
                    userActivityLevel = document.getString("nivelActividadFisica")

                    // Cargar equipamiento
                    firestore.collection("usuarios").document(userId).collection("equipamiento")
                        .whereEqualTo("estaSeleccionado", true)
                        .get()
                        .addOnSuccessListener { equipmentSnapshot ->
                            userEquipmentList = equipmentSnapshot.documents.mapNotNull { it.getString("nombre") }
                            loadRecommendedRoutines()
                        }
                        .addOnFailureListener {
                            loadRecommendedRoutines() // Cargar igual aunque falle el equipo
                        }
                } else {
                    loadRecommendedRoutines() // Cargar genéricas
                }
            }
            .addOnFailureListener {
                showLoading(false)
                if(context != null) Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecommendedRoutines() {
        var query: Query = firestore.collection("rutinas").limit(30)

        // 1. Filtro por Nivel
        val level = userLevel ?: "Principiante"
        query = when (level) {
            "Intermedio" -> query.whereIn("nivel", listOf("Intermedio", "Avanzado"))
            "Avanzado" -> query.whereEqualTo("nivel", "Avanzado")
            else -> query.whereIn("nivel", listOf("Principiante", "Intermedio", "Todos los Niveles"))
        }

        // 2. Filtro por Actividad (Opcional)
        userActivityLevel?.let { activityLevel ->
            if (activityLevel != "Todos los Niveles") {
                val adjacentLevels = getAdjacentActivityLevels(activityLevel)
                if (adjacentLevels.isNotEmpty()) {
                    // Nota: Firestore limita las consultas 'IN' a un solo campo.
                    // Si 'nivel' ya usa 'IN', no podemos usar otro 'IN' aquí.
                    // Por simplicidad y evitar crashes, priorizamos el filtro de NIVEL en la query
                    // y filtraremos actividad en memoria si es necesario.
                }
            }
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = documents.mapNotNull { doc ->
                    doc.toObject(Rutina::class.java).apply { id = doc.id }
                }

                // Filtrado avanzado en cliente (Kotlin)
                val finalRecommendations = fetchedRoutines.filter { rutina ->
                    // A. Filtro Equipamiento
                    val hasEquipment = checkEquipmentCompatibility(rutina)

                    // B. Filtro Salud
                    val isHealthy = isRoutineHealthCompatible(rutina)

                    hasEquipment && isHealthy
                }

                updateUI(finalRecommendations)
            }
            .addOnFailureListener {
                showLoading(false)
                Log.e("Recommendations", "Error loading routines", it)
            }
    }

    private fun checkEquipmentCompatibility(rutina: Rutina): Boolean {
        if (rutina.equipamiento.isNullOrEmpty()) return true
        if (rutina.equipamiento.size == 1 && rutina.equipamiento[0] == "Sin equipamiento") return true

        val userEquip = userEquipmentList.orEmpty().toSet()
        val routineEquip = rutina.equipamiento.toSet()

        // Si el usuario tiene AL MENOS UNO de los equipos requeridos, la mostramos (menos estricto)
        return routineEquip.any { it in userEquip }
    }

    private fun updateUI(rutinas: List<Rutina>) {
        showLoading(false)

        if (rutinas.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewRecommendations.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewRecommendations.visibility = View.VISIBLE
            recommendationsAdapter.updateList(rutinas)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewRecommendations.visibility = View.GONE
        }
    }

    private fun saveRoutine(rutina: Rutina) {
        val userId = auth.currentUser?.uid ?: return
        val routineId = rutina.id

        if (routineId.isNotEmpty()) {
            firestore.collection("usuarios")
                .document(userId)
                .collection("rutinasGuardadas")
                .document(routineId)
                .set(rutina)
                .addOnSuccessListener {
                    Toast.makeText(context, "¡${rutina.nombreRutina} guardada!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Helpers (Mantenemos tu lógica original encapsulada)
    private fun getAdjacentActivityLevels(level: String): List<String> {
        val all = listOf("Sedentario", "Moderado", "Activo", "Muy Activo")
        val idx = all.indexOf(level)
        if (idx == -1) return emptyList()

        val result = mutableListOf(level)
        if (idx > 0) result.add(all[idx - 1])
        if (idx < all.size - 1) result.add(all[idx + 1])
        return result
    }

    private fun isRoutineHealthCompatible(rutina: Rutina): Boolean {
        val restrictions = healthRestrictions?.get("restriccionesFisicas") as? Map<String, Boolean> ?: return true

        if (restrictions["dolorRodilla"] == true) {
            return !rutina.ejercicios.any {
                it.nombreEjercicio.contains("salto", true) ||
                        it.nombreEjercicio.contains("burpee", true)
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}