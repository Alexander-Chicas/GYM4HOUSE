package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore // Importar Firestore
import com.google.firebase.firestore.Query // Importar Query si es necesario para order by

class RutinasFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var textViewNombreRutina: TextView
    private lateinit var textViewDescripcionRutina: TextView
    private lateinit var textViewNivel: TextView
    private lateinit var textViewDuracion: TextView
    private lateinit var recyclerViewEjercicios: RecyclerView
    private lateinit var ejercicioAdapter: EjercicioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance() // Inicializar Firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rutinas, container, false)

        // Inicializar las vistas del layout
        textViewNombreRutina = view.findViewById(R.id.textViewNombreRutina)
        textViewDescripcionRutina = view.findViewById(R.id.textViewDescripcionRutina)
        textViewNivel = view.findViewById(R.id.textViewNivel)
        textViewDuracion = view.findViewById(R.id.textViewDuracion)
        recyclerViewEjercicios = view.findViewById(R.id.recyclerViewEjercicios)

        // Configurar RecyclerView
        recyclerViewEjercicios.layoutManager = LinearLayoutManager(context)

        // Llamar a la función para cargar la rutina
        loadCurrentRoutine()

        return view
    }

    private fun loadCurrentRoutine() {
        // En un escenario real, aquí buscarías la rutina específica del usuario.
        // Por ahora, leeremos el primer documento de la colección 'rutinas'.
        firestore.collection("rutinas")
            .limit(1) // Obtener solo el primer documento
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "No hay rutinas disponibles.", Toast.LENGTH_SHORT).show()
                    // Limpiar UI si no hay rutinas
                    textViewNombreRutina.text = "No hay rutina."
                    textViewDescripcionRutina.text = ""
                    textViewNivel.text = "Nivel: N/A"
                    textViewDuracion.text = "Duración: N/A"
                    ejercicioAdapter = EjercicioAdapter(emptyList())
                    recyclerViewEjercicios.adapter = ejercicioAdapter
                    return@addOnSuccessListener
                }

                // Asumimos que solo obtendremos un documento por el limit(1)
                val document = documents.first()
                val nombreRutina = document.getString("nombreRutina") ?: "Nombre Desconocido"
                val descripcion = document.getString("descripcion") ?: "Sin descripción."
                val nivel = document.getString("nivel") ?: "N/A"
                val duracion = document.getLong("duracionMinutos") ?: 0L

                // Actualizar los TextViews
                textViewNombreRutina.text = nombreRutina
                textViewDescripcionRutina.text = descripcion
                textViewNivel.text = "Nivel: $nivel"
                textViewDuracion.text = "Duración: ${duracion} minutos"

                // Obtener la lista de ejercicios
                val ejerciciosMapList = document.get("ejercicios") as? List<Map<String, Any>>
                val ejerciciosList = ejerciciosMapList?.map { Ejercicio.fromMap(it) } ?: emptyList()

                // Configurar y asignar el Adapter al RecyclerView
                ejercicioAdapter = EjercicioAdapter(ejerciciosList)
                recyclerViewEjercicios.adapter = ejercicioAdapter

            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar la rutina: ${exception.message}", Toast.LENGTH_LONG).show()
                // Puedes loguear el error para depuración
                // Log.e("RutinasFragment", "Error loading routine", exception)
            }
    }
}