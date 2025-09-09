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

    // 🟢 US-19
    private var ejercicioIndex = 0
    private var serieIndex = 0
    private var isResting = false
    private var timer: CountDownTimer? = null

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

        arguments?.getParcelable<Rutina>("rutina")?.let {
            rutinaActual = it
            setupUI()
            startWorkout()
            startNextExercise()
        } ?: run {
            Toast.makeText(context, "Error: Rutina no recibida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        binding.tvRoutineName.text = rutinaActual.nombreRutina
        binding.tvRoutineLevel.text = "Nivel: ${rutinaActual.nivel}"
        binding.chronometerWorkout.base = SystemClock.elapsedRealtime()
        binding.btnFinishWorkout.setOnClickListener { finishWorkout() }
    }

    private fun startWorkout() {
        workoutStartTime = SystemClock.elapsedRealtime()
        binding.chronometerWorkout.base = workoutStartTime
        binding.chronometerWorkout.start()
    }

    private fun startNextExercise() {
        if (ejercicioIndex >= rutinaActual.ejercicios.size) {
            finishWorkout()
            return
        }

        val ejercicio = rutinaActual.ejercicios[ejercicioIndex]

        // Animación al cambiar de ejercicio
        binding.tvExercisesTitle.animate().alpha(0f).setDuration(150).withEndAction {
            binding.tvExercisesTitle.text = "${ejercicio.nombreEjercicio}"
            binding.tvExercisesTitle.animate().alpha(1f).duration = 150
        }

        binding.tvSerieInfo.text = "Serie ${serieIndex + 1}/${ejercicio.series}"

        val color = if (!isResting) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800")
        binding.tvExercisesTitle.setTextColor(color)
        binding.tvSerieInfo.setTextColor(color)
        binding.chronometerWorkout.setTextColor(color)

        if (!isResting) {
            startTimer(ejercicio.repeticiones.toLong()) {
                isResting = true
                startNextExercise()
            }
        } else {
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
        timer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                val label = if (!isResting) "Ejecutando: $sec s" else "Descanso: $sec s"
                binding.tvRoutineDuration.text = label

                // Actualizar barra de progreso
                val progreso = ((seconds - sec).toFloat() / seconds * 100).toInt()
                binding.progressSeries.progress = progreso
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    private fun finishWorkout() {
        timer?.cancel()
        binding.chronometerWorkout.stop()
        val elapsedMillis = SystemClock.elapsedRealtime() - workoutStartTime
        val durationMinutes = elapsedMillis / (1000 * 60)

        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val ejerciciosParaGuardar = rutinaActual.ejercicios.map { ejercicio ->
            mapOf(
                "nombreEjercicio" to ejercicio.nombreEjercicio,
                "repeticiones" to ejercicio.repeticiones,
                "series" to ejercicio.series,
                "descansoSegundos" to ejercicio.descansoSegundos
            )
        }

        val historialEntrenamiento = HistorialRutina(
            id = firestore.collection("usuarios").document(userId)
                .collection("progreso").document().id,
            rutinaId = rutinaActual.id,
            nombreRutina = rutinaActual.nombreRutina,
            fechaCompletado = Timestamp.now(),
            duracionMinutos = durationMinutes,
            nivel = rutinaActual.nivel,
            ejerciciosRealizados = ejerciciosParaGuardar
        )

        GlobalScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("usuarios")
                    .document(userId)
                    .collection("progreso")
                    .document(historialEntrenamiento.id)
                    .set(historialEntrenamiento)
                    .await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Entrenamiento registrado", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.replaceFragment(RoutineHistoryFragment())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error guardando: ${e.message}", Toast.LENGTH_LONG).show()
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
