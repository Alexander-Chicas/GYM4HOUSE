package com.example.gym4house

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentWorkoutSessionBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WorkoutSessionFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionBinding? = null
    private val binding get() = _binding!!

    private lateinit var rutinaActual: Rutina
    private var workoutStartTime: Long = 0L

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Variables de estado
    private var ejercicioIndex = 0
    private var serieIndex = 0
    private var isResting = false
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var timeRemaining: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Recuperar la rutina
        arguments?.getParcelable<Rutina>("rutina")?.let {
            rutinaActual = it
            setupUI()
            startWorkout()
            startNextExercise()
        } ?: run {
            Toast.makeText(context, "Error: Rutina no recibida", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        binding.tvRoutineName.text = rutinaActual.nombreRutina
        binding.tvRoutineLevel.text = "Nivel: ${rutinaActual.nivel}"

        // Configurar botones
        binding.btnFinishWorkout.setOnClickListener { finishWorkout() }

        binding.btnPauseResume.setOnClickListener {
            togglePause()
        }

        binding.btnSkip.setOnClickListener {
            skipExercise()
        }
    }

    private fun startWorkout() {
        workoutStartTime = SystemClock.elapsedRealtime()
        // Aquí podríamos iniciar un cronómetro general para el tiempo total si quisiéramos
        binding.tvTotalTime.text = "00:00"
    }

    private fun startNextExercise() {
        if (ejercicioIndex >= rutinaActual.ejercicios.size) {
            finishWorkout()
            return
        }

        val ejercicio = rutinaActual.ejercicios[ejercicioIndex]

        // Animación de transición
        binding.cardCurrentExercise.animate().alpha(0f).translationY(20f).setDuration(200).withEndAction {
            // Actualizar textos
            binding.tvCurrentExerciseName.text = ejercicio.nombreEjercicio
            binding.tvSerieInfo.text = "Serie ${serieIndex + 1} de ${ejercicio.series}"

            // Reiniciar visuales
            binding.cardCurrentExercise.animate().alpha(1f).translationY(0f).setDuration(200).start()
        }

        // Configurar colores y estado
        if (!isResting) {
            binding.tvStatusLabel.text = "EJERCITANDO"
            binding.tvStatusLabel.setTextColor(Color.parseColor("#00E676")) // Verde Neón
            binding.tvTimerMain.setTextColor(Color.WHITE)

            // Para el ejercicio activo, usamos el tiempo estimado o repeticiones
            // Aquí asumimos un tiempo base por repetición si quieres timer, o solo mostramos info
            val tiempoEstimado = ejercicio.repeticiones * 3L // 3 segs por rep aprox
            startTimer(tiempoEstimado) {
                isResting = true
                startNextExercise()
            }
        } else {
            binding.tvStatusLabel.text = "DESCANSO"
            binding.tvStatusLabel.setTextColor(Color.parseColor("#FF9800")) // Naranja Neón
            binding.tvTimerMain.setTextColor(Color.parseColor("#FF9800"))

            startTimer(ejercicio.descansoSegundos.toLong()) {
                isResting = false
                serieIndex++
                if (serieIndex >= ejercicio.series) {
                    serieIndex = 0
                    ejercicioIndex++
                }
                startNextExercise()
            }
        }
    }

    private fun startTimer(seconds: Long, onFinish: () -> Unit) {
        timer?.cancel()
        val totalMillis = seconds * 1000
        binding.progressSeries.max = 1000 // Mayor resolución

        timer = object : CountDownTimer(totalMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val sec = millisUntilFinished / 1000
                binding.tvTimerMain.text = String.format("%02d:%02d", sec / 60, sec % 60)

                // Actualizar barra de progreso inversamente
                val progress = ((totalMillis - millisUntilFinished).toFloat() / totalMillis * 1000).toInt()
                binding.progressSeries.setProgress(progress, true)
            }

            override fun onFinish() {
                binding.progressSeries.progress = 1000
                onFinish()
            }
        }.start()
    }

    private fun togglePause() {
        if (isPaused) {
            isPaused = false
            binding.btnPauseResume.text = "PAUSAR"
            binding.btnPauseResume.icon = context?.getDrawable(R.drawable.ic_lock) // Icono pausa
            startTimer(timeRemaining / 1000) {
                // Lógica de continuación según estado (similar a startNextExercise)
                if (isResting) {
                    isResting = false
                    serieIndex++
                    // ... (Lógica simplificada para reanudar)
                    val ejercicio = rutinaActual.ejercicios[ejercicioIndex]
                    if (serieIndex >= ejercicio.series) {
                        serieIndex = 0
                        ejercicioIndex++
                    }
                    startNextExercise()
                } else {
                    isResting = true
                    startNextExercise()
                }
            }
        } else {
            isPaused = true
            timer?.cancel()
            binding.btnPauseResume.text = "REANUDAR"
            binding.btnPauseResume.icon = context?.getDrawable(R.drawable.ic_arrow_back) // Icono play
        }
    }

    private fun skipExercise() {
        timer?.cancel()
        isResting = false
        serieIndex = 0
        ejercicioIndex++
        startNextExercise()
    }

    private fun finishWorkout() {
        timer?.cancel()

        val elapsedMillis = SystemClock.elapsedRealtime() - workoutStartTime
        val durationMinutes = elapsedMillis / (1000 * 60)

        val userId = auth.currentUser?.uid ?: return

        // Preparar datos para guardar
        val ejerciciosParaGuardar = rutinaActual.ejercicios.map { ejercicio ->
            mapOf(
                "nombreEjercicio" to ejercicio.nombreEjercicio,
                "repeticiones" to ejercicio.repeticiones,
                "series" to ejercicio.series
            )
        }

        val historialEntrenamiento = HistorialRutina(
            id = firestore.collection("usuarios").document(userId).collection("progreso").document().id,
            rutinaId = rutinaActual.id,
            nombreRutina = rutinaActual.nombreRutina,
            fechaCompletado = Timestamp.now(),
            duracionMinutos = durationMinutes,
            nivel = rutinaActual.nivel,
            ejerciciosRealizados = ejerciciosParaGuardar
        )

        // Guardar en Firebase
        binding.btnFinishWorkout.isEnabled = false
        GlobalScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("usuarios")
                    .document(userId)
                    .collection("progreso")
                    .document(historialEntrenamiento.id)
                    .set(historialEntrenamiento)
                    .await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "¡Entrenamiento Completado!", Toast.LENGTH_LONG).show()
                    // Volver atrás o ir a resumen
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                    binding.btnFinishWorkout.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }

    companion object {
        fun newInstance(rutina: Rutina): WorkoutSessionFragment {
            return WorkoutSessionFragment().apply {
                arguments = Bundle().apply { putParcelable("rutina", rutina) }
            }
        }
    }
}