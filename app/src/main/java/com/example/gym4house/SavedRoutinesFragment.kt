package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

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

class SavedRoutinesFragment : Fragment(), RutinaAdapter.OnRoutineActionListener { // Implementamos el listener por si acaso

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewSavedRoutines: RecyclerView
    private lateinit var savedRoutinesAdapter: RutinaAdapter // Reutilizamos el mismo adaptador
    private val savedRoutinesList = mutableListOf<Rutina>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_routines, container, false)

        recyclerViewSavedRoutines = view.findViewById(R.id.recyclerViewSavedRoutines)
        recyclerViewSavedRoutines.layoutManager = LinearLayoutManager(context)

        // Pasamos 'this' como listener al adaptador, aunque aquí quizás el botón de guardar no tenga sentido
        // pero es necesario para que el adaptador funcione. Podríamos ocultar el botón si fuera necesario.
        savedRoutinesAdapter = RutinaAdapter(savedRoutinesList, this)
        recyclerViewSavedRoutines.adapter = savedRoutinesAdapter

        loadSavedRoutines() // Cargar las rutinas guardadas al iniciar

        return view
    }

    private fun loadSavedRoutines() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Debes iniciar sesión para ver tus rutinas guardadas.", Toast.LENGTH_SHORT).show()
            savedRoutinesList.clear()
            savedRoutinesAdapter.notifyDataSetChanged()
            return
        }

        firestore.collection("usuarios")
            .document(userId)
            .collection("rutinasGuardadas")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedRoutines = mutableListOf<Rutina>()
                for (document in documents) {
                    val rutina = document.toObject(Rutina::class.java)
                    rutina.id = document.id // Asegúrate de asignar el ID del documento
                    fetchedRoutines.add(rutina)
                }
                savedRoutinesAdapter.updateList(fetchedRoutines)

                if (fetchedRoutines.isEmpty()) {
                    Toast.makeText(context, "Aún no tienes rutinas guardadas.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar rutinas guardadas: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Implementación del método de la interfaz RutinaAdapter.OnRoutineActionListener
    // Aquí podrías decidir qué hacer si el usuario vuelve a tocar "Guardar" en una rutina ya guardada
    // Por ahora, mostraremos un Toast simple. Podrías implementar una lógica para "desguardar".
    override fun onSaveRoutineClick(rutina: Rutina) {
        Toast.makeText(context, "${rutina.nombreRutina} ya está en tus guardados.", Toast.LENGTH_SHORT).show()
        // O podrías agregar lógica para eliminarla de guardados si se vuelve a presionar
    }

    // Podrías recargar las rutinas si el fragmento vuelve a estar visible
    override fun onResume() {
        super.onResume()
        loadSavedRoutines()
    }
}