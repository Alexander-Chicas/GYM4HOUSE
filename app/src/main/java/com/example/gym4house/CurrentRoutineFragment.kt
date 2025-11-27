package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.FragmentCurrentRoutineBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CurrentRoutineFragment : Fragment(), FilterDialogFragment.FilterDialogListener {

    private var _binding: FragmentCurrentRoutineBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rutinaAdapter: RutinaAdapter
    private val rutinasList = mutableListOf<Rutina>()

    // Filtros actuales
    private var currentFilterTipo: String? = null
    private var currentFilterNivel: String? = null
    private var currentFilterDuracionMax: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrentRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupListeners()
        loadRoutines(null, null, null) // Carga inicial sin filtros
    }

    private fun setupRecyclerView() {
        // Inicializamos el adaptador usando la sintaxis moderna (Lambda)
        // Cuando se hace clic en una tarjeta:
        rutinaAdapter = RutinaAdapter(rutinasList) { rutinaSeleccionada ->
            // Acción al hacer clic: Guardar la rutina o ir al detalle
            saveRoutineToUser(rutinaSeleccionada)
        }

        binding.recyclerViewRutinasFiltradas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rutinaAdapter
        }
    }

    private fun setupListeners() {
        binding.buttonFiltrar.setOnClickListener {
            val dialog = FilterDialogFragment()
            dialog.setFilterDialogListener(this)
            dialog.show(childFragmentManager, "FilterDialog")
        }
    }

    // Callback del diálogo de filtros
    override fun onApplyFilters(tipo: String?, nivel: String?, duracionMax: Long?) {
        currentFilterTipo = tipo
        currentFilterNivel = nivel
        currentFilterDuracionMax = duracionMax

        loadRoutines(currentFilterTipo, currentFilterNivel, currentFilterDuracionMax)
        Toast.makeText(context, "Filtros aplicados", Toast.LENGTH_SHORT).show()
    }

    private fun saveRoutineToUser(rutina: Rutina) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Inicia sesión para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        // Usamos el ID de la rutina para evitar duplicados
        val routineId = rutina.id
        if (routineId.isEmpty()) {
            Toast.makeText(context, "Error: Rutina sin ID", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("rutinasGuardadas")
            .document(routineId)
            .set(rutina)
            .addOnSuccessListener {
                Toast.makeText(context, "¡${rutina.nombreRutina} guardada en tu perfil!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRoutines(tipo: String?, nivel: String?, duracionMax: Long?) {
        var query: Query = firestore.collection("rutinas")

        // Aplicar filtros si existen
        if (!tipo.isNullOrEmpty()) query = query.whereEqualTo("tipo", tipo)
        if (!nivel.isNullOrEmpty()) query = query.whereEqualTo("nivel", nivel)
        if (duracionMax != null) query = query.whereLessThanOrEqualTo("duracionMinutos", duracionMax)

        query.get()
            .addOnSuccessListener { documents ->
                val newRutinas = documents.mapNotNull { doc ->
                    doc.toObject(Rutina::class.java).apply { id = doc.id }
                }.toMutableList()

                rutinaAdapter.updateList(newRutinas)

                // Manejo visual de estado vacío
                if (newRutinas.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewRutinasFiltradas.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.recyclerViewRutinasFiltradas.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error cargando rutinas", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}