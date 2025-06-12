package com.example.gym4house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Importar Query para ordenar

class RecommendationsFragment : Fragment(), RutinaAdapter.OnRoutineActionListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewRecommendations: RecyclerView
    private lateinit var recommendationsAdapter: RutinaAdapter
    private val recommendedRoutinesList = mutableListOf<Rutina>()

    private var userLevel: String? = null // Para almacenar el nivel del usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fetchUserLevel() // Obtener el nivel del usuario al crear el fragmento
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

        // Las recomendaciones se cargarán después de obtener el nivel del usuario
        // (dentro de fetchUserLevel o en onResume para recargar si cambia el nivel)

        return view
    }

    // Método para obtener el nivel del usuario desde Firestore
    private fun fetchUserLevel() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Inicia sesión para recibir recomendaciones personalizadas.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtenemos el nivel del usuario del campo 'nivelExperiencia'
                    userLevel = document.getString("nivelExperiencia")
                    loadRecommendedRoutines() // Una vez que tenemos el nivel, cargamos las rutinas
                } else {
                    Toast.makeText(context, "No se encontró el perfil de usuario. Cargando recomendaciones generales.", Toast.LENGTH_SHORT).show()
                    loadRecommendedRoutines() // Cargar sin nivel específico si no se encuentra
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al obtener nivel de usuario: ${e.message}", Toast.LENGTH_LONG).show()
                loadRecommendedRoutines() // Cargar sin nivel específico si hay un error
            }
    }

    // Lógica para cargar rutinas recomendadas
    private fun loadRecommendedRoutines() {
        var query: Query = firestore.collection("rutinas")

        // Lógica de recomendación simple basada en el nivel del usuario
        // USAMOS "nivel" AQUÍ PORQUE ASÍ SE LLAMA EL CAMPO EN LOS DOCUMENTOS DE RUTINA
        userLevel?.let { level ->
            when (level) {
                "Principiante" -> {
                    // Recomendar Principiante o Intermedio
                    query = query.whereIn("nivel", listOf("Principiante", "Intermedio"))
                }
                "Intermedio" -> {
                    // Recomendar Intermedio o Avanzado
                    query = query.whereIn("nivel", listOf("Intermedio", "Avanzado"))
                }
                "Avanzado" -> {
                    // Recomendar solo Avanzado
                    query = query.whereEqualTo("nivel", "Avanzado")
                }
                else -> {
                    // Nivel desconocido o no definido, recomendar principiante
                    query = query.whereEqualTo("nivel", "Principiante")
                    Toast.makeText(context, "Nivel de usuario no definido, mostrando rutinas para Principiantes.", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            // Si no hay nivel de usuario, mostrar recomendaciones para principiantes por defecto
            query = query.whereEqualTo("nivel", "Principiante")
            Toast.makeText(context, "Nivel de usuario no disponible, mostrando rutinas para Principiantes.", Toast.LENGTH_SHORT).show()
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = mutableListOf<Rutina>()
                for (document in documents) {
                    val rutina = document.toObject(Rutina::class.java)
                    rutina.id = document.id
                    fetchedRoutines.add(rutina)
                }
                recommendedRoutinesList.clear()
                recommendedRoutinesList.addAll(fetchedRoutines)
                recommendationsAdapter.notifyDataSetChanged()

                if (fetchedRoutines.isEmpty()) {
                    Toast.makeText(context, "No hay recomendaciones disponibles para tu nivel.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar recomendaciones: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Reimplementar onSaveRoutineClick para manejar guardar desde recomendaciones
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

    // Para asegurar que las recomendaciones se recarguen si el usuario cambia de pestaña
    override fun onResume() {
        super.onResume()
        fetchUserLevel() // Volver a obtener el nivel y cargar recomendaciones al volver
    }
}