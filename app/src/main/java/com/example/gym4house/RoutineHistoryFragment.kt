package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth // Para obtener el UID del usuario
import com.google.firebase.firestore.FirebaseFirestore // Para interactuar con Firestore

class RoutineHistoryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewHistorial: RecyclerView
    private lateinit var historialAdapter: HistorialRutinaAdapter
    private val historialList = mutableListOf<HistorialRutina>() // Lista mutable para los datos

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

        recyclerViewHistorial = view.findViewById(R.id.recyclerViewHistorial) // ¡Añadiremos este ID al XML!
        recyclerViewHistorial.layoutManager = LinearLayoutManager(context)

        historialAdapter = HistorialRutinaAdapter(historialList)
        recyclerViewHistorial.adapter = historialAdapter

        loadRoutineHistory() // Cargar el historial cuando la vista es creada

        return view
    }

    private fun loadRoutineHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("historialrutinas")
            .orderBy("fechaCompletado", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por fecha, los más recientes primero
            .get()
            .addOnSuccessListener { documents ->
                historialList.clear() // Limpiar la lista antes de añadir nuevos datos
                for (document in documents) {
                    val historial = document.toObject(HistorialRutina::class.java)
                    historialList.add(historial)
                }
                historialAdapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado

                if (historialList.isEmpty()) {
                    Toast.makeText(context, "No hay historial de rutinas.", Toast.LENGTH_SHORT).show()
                    // Si tienes un TextView de "No hay historial", puedes mostrarlo aquí
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar el historial: ${exception.message}", Toast.LENGTH_LONG).show()
                // Log.e("RoutineHistory", "Error loading history", exception)
            }
    }
}