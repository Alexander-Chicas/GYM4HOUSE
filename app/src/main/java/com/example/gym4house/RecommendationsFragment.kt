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
        fetchUserProfile() // Se carga el perfil completo al entrar al fragmento
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

                    // OJO AQUÍ: Solo se obtienen los documentos donde 'estaSeleccionado' es true
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
                Toast.makeText(context, "Error al obtener perfil de usuario: ${e.message}", Toast.LENGTH_LONG).show()
                loadRecommendedRoutines()
            }
    }

    private fun loadRecommendedRoutines() {
        Log.d("RecommendationsFragment", "Iniciando la carga de recomendaciones...")

        var query: Query = firestore.collection("rutinas")
            .limit(30)

        val level = userLevel ?: "Principiante"
        query = when (level) {
            "Intermedio" -> query.whereIn("nivel", listOf("Intermedio", "Avanzado"))
            "Avanzado" -> query.whereEqualTo("nivel", "Avanzado")
            else -> query.whereIn("nivel", listOf("Principiante", "Intermedio", "Todos los Niveles"))
        }

        preferredExerciseType?.let { type ->
            if (type != "Todos los Tipos") {
                query = query.whereEqualTo("tipo", type)
            }
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = documents.toObjects(Rutina::class.java).toMutableList()
                Log.d("RecommendationsFragment", "Rutinas cargadas antes de filtrar: ${fetchedRoutines.size}")

                val finalRecommendations = fetchedRoutines.filter { rutina ->
                    val hasRequiredEquipment = if (rutina.equipamiento.isNullOrEmpty() || rutina.equipamiento?.contains("Sin equipamiento") == true) {
                        true
                    } else {
                        rutina.equipamiento.orEmpty().any { it in userEquipmentList.orEmpty() }
                    }

                    val isHealthCompatible = isRoutineHealthCompatible(rutina)

                    hasRequiredEquipment && isHealthCompatible
                }

                recommendedRoutinesList.clear()
                recommendedRoutinesList.addAll(finalRecommendations)
                recommendationsAdapter.notifyDataSetChanged()

                if (finalRecommendations.isEmpty()) {
                    Toast.makeText(context, "No hay recomendaciones que coincidan con tus preferencias y equipamiento.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar recomendaciones: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isRoutineHealthCompatible(rutina: Rutina): Boolean {
        if (healthRestrictions == null) return true

        val hasKneePain = (healthRestrictions?.get("restriccionesFisicas") as? Map<String, Any>)?.get("dolorRodilla") as? Boolean ?: false
        if (hasKneePain) {
            val hasHighImpactExercises = rutina.ejercicios?.any {
                it.nombreEjercicio?.contains("salto", ignoreCase = true) == true ||
                        it.nombreEjercicio?.contains("burpees", ignoreCase = true) == true ||
                        it.nombreEjercicio?.contains("zancada", ignoreCase = true) == true
            } ?: false
            if (hasHighImpactExercises) {
                Log.d("HealthFilter", "Descartando rutina '${rutina.nombreRutina}' por dolor de rodilla.")
                return false
            }
        }

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