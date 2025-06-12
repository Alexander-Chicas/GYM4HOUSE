package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth // Importar FirebaseAuth para el UID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Importar Query para las consultas

class CurrentRoutineFragment : Fragment(),
    FilterDialogFragment.FilterDialogListener, // Ya implementada
    RutinaAdapter.OnRoutineActionListener { // ¡NUEVO! Implementa esta interfaz

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth // Añadir referencia a FirebaseAuth
    private lateinit var recyclerViewRutinas: RecyclerView
    private lateinit var rutinaAdapter: RutinaAdapter
    private val rutinasList = mutableListOf<Rutina>()

    private lateinit var buttonFiltrar: Button

    // Variables para almacenar los filtros actuales
    private var currentFilterTipo: String? = null
    private var currentFilterNivel: String? = null
    private var currentFilterDuracionMax: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_routine, container, false)

        recyclerViewRutinas = view.findViewById(R.id.recyclerViewRutinasFiltradas)
        recyclerViewRutinas.layoutManager = LinearLayoutManager(context)

        // Pasar 'this' como listener al adaptador
        rutinaAdapter = RutinaAdapter(rutinasList, this) // <-- ¡CAMBIO AQUÍ!
        recyclerViewRutinas.adapter = rutinaAdapter

        buttonFiltrar = view.findViewById(R.id.buttonFiltrar)
        buttonFiltrar.setOnClickListener {
            showFilterDialog()
        }

        loadRoutines(currentFilterTipo, currentFilterNivel, currentFilterDuracionMax)

        return view
    }

    private fun showFilterDialog() {
        val dialog = FilterDialogFragment()
        dialog.setFilterDialogListener(this)
        dialog.show(childFragmentManager, "FilterDialog")
    }

    // --- Implementación del método de FilterDialogListener ---
    override fun onApplyFilters(tipo: String?, nivel: String?, duracionMax: Long?) {
        currentFilterTipo = tipo
        currentFilterNivel = nivel
        currentFilterDuracionMax = duracionMax
        loadRoutines(currentFilterTipo, currentFilterNivel, currentFilterDuracionMax)
        Toast.makeText(context, "Filtros aplicados.", Toast.LENGTH_SHORT).show()
    }

    // --- Implementación del método de RutinaAdapter.OnRoutineActionListener ---
    override fun onSaveRoutineClick(rutina: Rutina) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Debes iniciar sesión para guardar rutinas.", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencia a la subcolección donde se guardarán las rutinas del usuario
        val savedRoutinesRef = firestore.collection("usuarios")
            .document(userId)
            .collection("rutinasGuardadas")

        // Para evitar duplicados y facilitar el acceso, usaremos el ID de la rutina como ID del documento
        // en la subcolección del usuario.
        rutina.id?.let { routineId ->
            savedRoutinesRef.document(routineId)
                .set(rutina) // Guarda el objeto Rutina completo
                .addOnSuccessListener {
                    Toast.makeText(context, "${rutina.nombreRutina} guardada correctamente.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar rutina: ${e.message}", Toast.LENGTH_LONG).show()
                    // Log.e("SaveRoutine", "Error saving routine", e)
                }
        } ?: run {
            Toast.makeText(context, "No se pudo guardar la rutina (ID no disponible).", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadRoutines(tipo: String?, nivel: String?, duracionMax: Long?) {
        var query: Query = firestore.collection("rutinas")

        tipo?.let {
            query = query.whereEqualTo("tipo", it)
        }
        nivel?.let {
            query = query.whereEqualTo("nivel", it)
        }
        duracionMax?.let {
            query = query.whereLessThanOrEqualTo("duracionMinutos", it)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val fetchedRutinas = mutableListOf<Rutina>()
                for (document in documents) {
                    val rutina = document.toObject(Rutina::class.java)
                    // Asegúrate de que el ID del documento de Firestore se asigne a la propiedad 'id' de Rutina
                    // Esto es crucial para la lógica de guardado y futura identificación.
                    rutina.id = document.id // Asigna el ID del documento de Firestore
                    fetchedRutinas.add(rutina)
                }
                rutinaAdapter.updateList(fetchedRutinas)

                if (fetchedRutinas.isEmpty()) {
                    Toast.makeText(context, "No hay rutinas disponibles con los filtros seleccionados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar las rutinas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}