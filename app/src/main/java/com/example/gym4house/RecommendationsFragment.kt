package com.example.gym4house

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class RecommendationsFragment : Fragment(), RutinaAdapter.OnRoutineActionListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewRecommendations: RecyclerView
    private lateinit var recommendationsAdapter: RutinaAdapter
    private val recommendedRoutinesList = mutableListOf<Rutina>()

    // Variables para almacenar los nuevos datos del usuario
    private var userLevel: String? = null
    private var preferredExerciseType: String? = null
    private var userEquipmentList: List<String>? = null
    private var healthRestrictions: Map<String, Any>? = null
    private var userActivityLevel: String? = null // NUEVO: Nivel de actividad física

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommendations, container, false)

        recyclerViewRecommendations = view.findViewById(R.id.recyclerViewRecommendations)
        recyclerViewRecommendations.layoutManager = LinearLayoutManager(context)

        recommendationsAdapter = RutinaAdapter(recommendedRoutinesList, this)
        recyclerViewRecommendations.adapter = recommendationsAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Inicia sesión para recibir recomendaciones personalizadas.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RecommendationsFragment", "Fetching full user profile for userId: $userId")

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtener todos los campos relevantes del usuario
                    userLevel = document.getString("nivelExperiencia")
                    preferredExerciseType = document.getString("tipo_ejercicio_preferido")
                    healthRestrictions = document.get("restriccionesSalud") as? Map<String, Any>
                    userActivityLevel = document.getString("nivelActividadFisica") // NUEVO

                    firestore.collection("usuarios").document(userId).collection("equipamiento")
                        .whereEqualTo("estaSeleccionado", true)
                        .get()
                        .addOnSuccessListener { equipmentSnapshot ->
                            userEquipmentList = equipmentSnapshot.documents.mapNotNull { it.getString("nombre") }
                            Log.d("RecommendationsFragment", "Equipo cargado de Firestore: $userEquipmentList")
                            loadRecommendedRoutines()
                        }
                        .addOnFailureListener { e ->
                            Log.e("RecommendationsFragment", "Error al obtener equipamiento: ${e.message}")
                            loadRecommendedRoutines()
                        }

                } else {
                    Toast.makeText(context, "No se encontró el perfil. Cargando recomendaciones generales.", Toast.LENGTH_SHORT).show()
                    loadRecommendedRoutines()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this.context, "Error al obtener perfil de usuario: ${e.message}", Toast.LENGTH_LONG).show()
                loadRecommendedRoutines()
            }
    }

    private fun loadRecommendedRoutines() {
        Log.d("RecommendationsFragment", "Iniciando la carga de recomendaciones...")

        var query: Query = firestore.collection("rutinas")
            .limit(30)

        // 1. Filtrar por Nivel de Experiencia
        val level = userLevel ?: "Principiante"
        query = when (level) {
            "Intermedio" -> query.whereIn("nivel", listOf("Intermedio", "Avanzado"))
            "Avanzado" -> query.whereEqualTo("nivel", "Avanzado")
            else -> query.whereIn("nivel", listOf("Principiante", "Intermedio", "Todos los Niveles"))
        }
        Log.d("RecommendationsFragment", "Filtering by level: $level")


        // 2. Filtrar por Tipo de Ejercicio Preferido
        preferredExerciseType?.let { type ->
            if (type != "Todos los Tipos") {
                // Eliminamos el filtro estricto por tipo de ejercicio de la consulta de Firestore
                // para hacer las recomendaciones menos agresivas.
                Log.d("RecommendationsFragment", "Removing strict Firestore filter for preferred exercise type '$type' for less aggressive recommendations.")
                // query = query.whereEqualTo("tipo", type) // Línea eliminada/comentada
            }
        }

        // 3. Filtrar por Nivel de Actividad Física (usando niveles adyacentes)
        userActivityLevel?.let { activityLevel ->
            if (activityLevel != "Todos los Niveles") {
                val adjacentActivityLevels = getAdjacentActivityLevels(activityLevel)
                query = query.whereIn("nivelActividadFisica", adjacentActivityLevels)
                Log.d("RecommendationsFragment", "Filtering by user activity levels: $adjacentActivityLevels (adjusted from $activityLevel)")
            }
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = documents.map { document ->
                    val rutina = document.toObject(Rutina::class.java)
                    rutina.id = document.id // Asignar el ID del documento
                    rutina
                }.toMutableList()
                Log.d("RecommendationsFragment", "Rutinas cargadas antes de filtrar en cliente: ${fetchedRoutines.size}")

                val finalRecommendations = fetchedRoutines.filter { rutina ->
                    val rutinaIdForLog = rutina.nombreRutina ?: rutina.id ?: "Unknown"

                    // --- FILTRADO POR EQUIPAMIENTO (menos agresivo) ---
                    val hasRequiredEquipment = if (rutina.equipamiento.isNullOrEmpty() || (rutina.equipamiento?.size == 1 && rutina.equipamiento?.first() == "Sin equipamiento")) {
                        Log.d("EquipmentFilter", "Routine '$rutinaIdForLog' requires no specific equipment or explicitly 'Sin equipamiento'. Compatible.")
                        true
                    } else {
                        val userEquipmentSet = userEquipmentList.orEmpty().toSet()
                        val routineEquipmentSet = rutina.equipamiento.orEmpty().toSet()

                        val userHasAnyRequiredEquipment = routineEquipmentSet.any { requiredItem ->
                            userEquipmentSet.contains(requiredItem)
                        }

                        if (userHasAnyRequiredEquipment) {
                            Log.d("EquipmentFilter", "Routine '$rutinaIdForLog' is compatible (user has at least one of the required equipment: $routineEquipmentSet).")
                            true
                        } else {
                            Log.d("EquipmentFilter", "Discarding routine '$rutinaIdForLog' (user has none of the required equipment: $routineEquipmentSet). User equipment: $userEquipmentSet")
                            false
                        }
                    }

                    // --- FILTRADO POR RESTRICCIONES DE SALUD ---
                    val isHealthCompatible = isRoutineHealthCompatible(rutina)
                    if (!isHealthCompatible) {
                        Log.d("HealthFilter", "Discarding routine '$rutinaIdForLog' due to health restrictions.")
                    }

                    hasRequiredEquipment && isHealthCompatible
                }

                recommendedRoutinesList.clear()
                recommendedRoutinesList.addAll(finalRecommendations)
                recommendationsAdapter.notifyDataSetChanged()

                if (finalRecommendations.isEmpty()) {
                    Toast.makeText(context, "No hay recomendaciones que coincidan con tus preferencias y equipamiento.", Toast.LENGTH_LONG).show()
                    Log.w("RecommendationsFragment", "No final recommendations after client-side filtering.")
                } else {
                    Log.d("RecommendationsFragment", "Rutinas finales después de filtrar: ${finalRecommendations.size} rutinas.")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar recomendaciones: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("RecommendationsFragment", "Error loading recommendations from Firestore", exception)
            }
    }

    // Lógica para obtener niveles de actividad física adyacentes
    private fun getAdjacentActivityLevels(userActivityLevel: String): List<String> {
        val allLevels = listOf("Sedentario", "Moderado", "Activo", "Muy Activo")
        val userLevelIndex = allLevels.indexOf(userActivityLevel)

        val adjacentLevels = mutableListOf<String>()
        if (userLevelIndex != -1) {
            // Nivel del usuario
            adjacentLevels.add(userActivityLevel)

            // Nivel inferior (si existe)
            if (userLevelIndex > 0) {
                adjacentLevels.add(allLevels[userLevelIndex - 1])
            }
            // Nivel superior (si existe)
            if (userLevelIndex < allLevels.size - 1) {
                adjacentLevels.add(allLevels[userLevelIndex + 1])
            }
        }
        return adjacentLevels.distinct() // Eliminar duplicados y asegurar orden
    }

    private fun isRoutineHealthCompatible(rutina: Rutina): Boolean {
        if (healthRestrictions == null) return true

        val rutinaIdForLog = rutina.nombreRutina ?: rutina.id ?: "Unknown"

        val hasKneePain = (healthRestrictions?.get("restriccionesFisicas") as? Map<String, Any>)?.get("dolorRodilla") as? Boolean ?: false
        if (hasKneePain) {
            val hasHighImpactExercises = rutina.ejercicios?.any { ejercicio ->
                ejercicio.nombreEjercicio?.contains("salto", ignoreCase = true) == true ||
                        ejercicio.nombreEjercicio?.contains("burpees", ignoreCase = true) == true ||
                        ejercicio.nombreEjercicio?.contains("zancada", ignoreCase = true) == true
            } ?: false
            if (hasHighImpactExercises) {
                Log.d("HealthFilter", "Descartando rutina '${rutina.nombreRutina}' por dolor de rodilla (contiene ejercicios de alto impacto).")
                return false
            }
        }

        // Puedes agregar más lógica de filtrado aquí para otras condiciones...
        return true
    }

    override fun onSaveRoutineClick(rutina: Rutina) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Debes iniciar sesión para guardar rutinas.", Toast.LENGTH_SHORT).show()
            return
        }

        val savedRoutinesRef = firestore.collection("usuarios")
            .document(userId)
            .collection("rutinasGuardadas")

        rutina.id?.let { routineId ->
            savedRoutinesRef.document(routineId)
                .set(rutina)
                .addOnSuccessListener {
                    Toast.makeText(context, "${rutina.nombreRutina} guardada correctamente.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar rutina: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } ?: run {
            Toast.makeText(context, "No se pudo guardar la rutina (ID no disponible).", Toast.LENGTH_SHORT).show()
        }
    }
}