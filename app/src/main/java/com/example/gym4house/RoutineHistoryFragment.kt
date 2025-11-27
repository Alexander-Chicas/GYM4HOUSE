package com.example.gym4house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView // Importar TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RoutineHistoryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewHistorial: RecyclerView
    private lateinit var tvEmptyState: TextView // Texto para cuando no hay datos
    private lateinit var historialAdapter: HistorialRutinaAdapter
    private val historialList = mutableListOf<HistorialRutina>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_routine_history, container, false)

        recyclerViewHistorial = view.findViewById(R.id.recyclerViewHistorial)
        tvEmptyState = view.findViewById(R.id.tvEmptyState) // ID agregado en el XML nuevo

        recyclerViewHistorial.layoutManager = LinearLayoutManager(context)
        historialAdapter = HistorialRutinaAdapter(historialList)
        recyclerViewHistorial.adapter = historialAdapter

        loadRoutineHistory()

        return view
    }

    private fun loadRoutineHistory() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .orderBy("fechaCompletado", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                historialList.clear()
                for (document in documents) {
                    val historial = document.toObject(HistorialRutina::class.java)
                    historialList.add(historial)
                }
                historialAdapter.notifyDataSetChanged()

                // Mostrar/Ocultar mensaje de "Vacío"
                if (historialList.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerViewHistorial.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerViewHistorial.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                if(context != null) Toast.makeText(context, "Error cargando historial", Toast.LENGTH_SHORT).show()
            }
    }
}