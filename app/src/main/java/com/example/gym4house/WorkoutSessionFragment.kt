package com.example.gym4house

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentWorkoutSessionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp

class WorkoutSessionFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionBinding? = null
    private val binding get() = _binding!!
    private lateinit var rutinaActual: Rutina
    private var workoutStartTime: Long = 0L

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("WorkoutSession", "onCreateView: Iniciando creación de la vista.")
        _binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("WorkoutSession", "onViewCreated: Vista creada y configurando componentes.")

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        arguments?.getParcelable<Rutina>(ARG_RUTINA)?.let {
            rutinaActual = it
            setupUI()
            startWorkout()
        } ?: run {
            Log.e("WorkoutSession", "ERROR: Rutina no recibida en onViewCreated.")
            Toast.makeText(context, "Error: Rutina no recibida", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.replaceFragment(HomeFragment())
        }
    }

    private fun setupUI() {
        binding.tvRoutineName.text = rutinaActual.nombreRutina
        binding.tvRoutineLevel.text = "Nivel: ${rutinaActual.nivel}"
        binding.tvRoutineDuration.text = "Duración: ${rutinaActual.duracionMinutos} min"

        Log.d("WorkoutSession", "Número de ejercicios: ${rutinaActual.ejercicios.size}")

        binding.recyclerViewExercises.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = EjercicioAdapter(rutinaActual.ejercicios)
        }

        binding.btnFinishWorkout.setOnClickListener {
            finishWorkout()
        }
    }

    private fun startWorkout() {
        workoutStartTime = SystemClock.elapsedRealtime()
        binding.chronometerWorkout.base = workoutStartTime
        binding.chronometerWorkout.start()
        Log.d("WorkoutSession", "startWorkout: Cronómetro iniciado en ${workoutStartTime}ms.")
    }

    private fun finishWorkout() {
        Log.d("WorkoutSession", "--- finishWorkout() ha sido llamado. ---")

        binding.chronometerWorkout.stop()
        val elapsedMillis = SystemClock.elapsedRealtime() - workoutStartTime
        val durationMinutes = elapsedMillis / (1000 * 60)
        Log.d("WorkoutSession", "finishWorkout: Duración del entrenamiento: ${durationMinutes} minutos.")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("WorkoutSession", "ERROR: userId es nulo al finalizar entrenamiento.")
            Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.replaceFragment(HomeFragment())
            return
        }

        if (!::rutinaActual.isInitialized) {
            Log.e("WorkoutSession", "ERROR: rutinaActual no está inicializada al finalizar entrenamiento.")
            Toast.makeText(context, "Error: No hay rutina activa para finalizar.", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.replaceFragment(HomeFragment())
            return
        }

        // --- Mapeo de datos para guardar en HistorialRutina ---
        // Los campos repeticiones, series y descansoSegundos ya son Long en la clase Ejercicio,
        // por lo que se pueden asignar directamente.
        val ejerciciosParaGuardar = rutinaActual.ejercicios.map { ejercicio ->
            mapOf(
                "nombreEjercicio" to ejercicio.nombreEjercicio,
                "repeticiones" to ejercicio.repeticiones,
                "series" to ejercicio.series,
                "descansoSegundos" to ejercicio.descansoSegundos
            )
        }
        Log.d("WorkoutSession", "finishWorkout: Ejercicios a guardar (mapeados): $ejerciciosParaGuardar")
        // ---------------------------------

        val historialEntrenamiento = HistorialRutina(
            id = firestore.collection("usuarios").document(userId).collection("progreso").document().id, // Generar ID único
            rutinaId = rutinaActual.id,
            nombreRutina = rutinaActual.nombreRutina,
            fechaCompletado = Timestamp.now(),
            duracionMinutos = durationMinutes,
            nivel = rutinaActual.nivel,
            ejerciciosRealizados = ejerciciosParaGuardar
        )
        Log.d("WorkoutSession", "finishWorkout: Objeto HistorialRutina creado: $historialEntrenamiento")

        saveWorkoutHistory(userId, historialEntrenamiento)
    }

    private fun saveWorkoutHistory(userId: String, historialEntrenamiento: HistorialRutina) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("WorkoutSession", "saveWorkoutHistory: Intentando guardar historial en Firestore para usuario $userId.")
                Log.d("WorkoutSession", "saveWorkoutHistory: Datos a guardar: $historialEntrenamiento")

                firestore.collection("usuarios")
                    .document(userId)
                    .collection("progreso")
                    .document(historialEntrenamiento.id)
                    .set(historialEntrenamiento)
                    .await()

                Log.d("WorkoutSession", "saveWorkoutHistory: Progreso guardado exitosamente en Firestore.")

                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Entrenamiento finalizado y registrado!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.replaceFragment(RoutineHistoryFragment())
                }
            } catch (e: Exception) {
                Log.e("WorkoutSession", "ERROR CRÍTICO al guardar historial: ${e.message}", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar historial: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("WorkoutSession", "onDestroyView: Destruyendo la vista.")
        _binding = null
    }

    companion object {
        private const val ARG_RUTINA = "rutina"

        fun newInstance(rutina: Rutina): WorkoutSessionFragment {
            val fragment = WorkoutSessionFragment()
            val args = Bundle().apply {
                putParcelable(ARG_RUTINA, rutina)
            }
            fragment.arguments = args
            Log.d("WorkoutSession", "newInstance: Creada nueva instancia de WorkoutSessionFragment con rutina: ${rutina.nombreRutina}")
            return fragment
        }
    }
}