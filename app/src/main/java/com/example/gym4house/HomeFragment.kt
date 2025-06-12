package com.example.gym4house

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvGreeting: TextView
    private lateinit var tvMotivationalQuote: TextView
    private lateinit var tvNextReminder: TextView
    private lateinit var btnEditReminders: Button
    private lateinit var btnStartWorkout: Button
    private lateinit var btnMyRoutines: Button
    private lateinit var btnMyProgress: Button
    private lateinit var btnExploreExercises: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvMotivationalQuote = view.findViewById(R.id.tvMotivationalQuote)
        tvNextReminder = view.findViewById(R.id.tvNextReminder)
        btnEditReminders = view.findViewById(R.id.btnEditReminders)
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout)
        btnMyRoutines = view.findViewById(R.id.btnMyRoutines)
        btnMyProgress = view.findViewById(R.id.btnMyProgress)
        btnExploreExercises = view.findViewById(R.id.btnExploreExercises)

        loadUserData()
        loadNextReminder()

        // --- CAMBIO AQUÍ: Usar 'replaceFragment' de MainActivity ---
        btnEditReminders.setOnClickListener {
            // Se asume que MainActivity tiene una función 'replaceFragment' que toma un Fragment
            (activity as? MainActivity)?.replaceFragment(RecordatoriosFragment())
            Log.d("HomeFragment", "Botón 'Ver / Editar Recordatorios' pulsado.")
        }

        btnStartWorkout.setOnClickListener {
            Log.d("HomeFragment", "Botón 'Iniciar Entrenamiento' pulsado.")
            // Aquí podrías navegar a un nuevo fragmento o actividad para iniciar un entrenamiento
            // (activity as? MainActivity)?.replaceFragment(WorkoutSelectionFragment()) // Ejemplo
        }

        btnMyRoutines.setOnClickListener {
            Log.d("HomeFragment", "Botón 'Mis Rutinas' pulsado.")
            // (activity as? MainActivity)?.replaceFragment(MyRoutinesFragment()) // Ejemplo
        }

        btnMyProgress.setOnClickListener {
            Log.d("HomeFragment", "Botón 'Mi Progreso' pulsado.")
            // (activity as? MainActivity)?.replaceFragment(MyProgressFragment()) // Ejemplo
        }

        btnExploreExercises.setOnClickListener {
            Log.d("HomeFragment", "Botón 'Explorar Ejercicios' pulsado.")
            // (activity as? MainActivity)?.replaceFragment(ExploreExercisesFragment()) // Ejemplo
        }
        // --- FIN DEL CAMBIO ---

        return view
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("nombre")
                        tvGreeting.text = "¡Hola, ${name ?: "Usuario"}!"
                    } else {
                        tvGreeting.text = "¡Hola, Usuario!"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error al cargar datos del usuario: ${e.message}")
                    tvGreeting.text = "¡Hola, Usuario!"
                }
        } else {
            tvGreeting.text = "¡Hola, Usuario!"
        }
    }

    private fun loadNextReminder() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("recordatorios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val hora = document.getString("hora")
                        val diasSeleccionados = document.get("dias") as? List<String>

                        if (hora != null && !diasSeleccionados.isNullOrEmpty()) {
                            val nextReminderText = getNextReminderText(hora, diasSeleccionados)
                            tvNextReminder.text = nextReminderText
                        } else {
                            tvNextReminder.text = "No hay recordatorios configurados."
                        }
                    } else {
                        tvNextReminder.text = "No hay recordatorios configurados."
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error al cargar recordatorios: ${e.message}")
                    tvNextReminder.text = "Error al cargar recordatorios."
                }
        } else {
            tvNextReminder.text = "Inicia sesión para ver tus recordatorios."
        }
    }

    private fun getNextReminderText(hora: String, diasSeleccionados: List<String>): String {
        val dayMapping = mapOf(
            "Lunes" to Calendar.MONDAY,
            "Martes" to Calendar.TUESDAY,
            "Miércoles" to Calendar.WEDNESDAY,
            "Jueves" to Calendar.THURSDAY,
            "Viernes" to Calendar.FRIDAY,
            "Sábado" to Calendar.SATURDAY,
            "Domingo" to Calendar.SUNDAY
        )

        val currentTime = Calendar.getInstance()
        val currentDayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK)

        var minTimeDiffMillis = Long.MAX_VALUE
        var nextReminderDayName = ""
        var nextReminderTime: Calendar? = null

        val partesHora = hora.split(":")
        val alarmaHora = partesHora[0].toInt()
        val alarmaMinuto = partesHora[1].toInt()

        for (diaNombre in diasSeleccionados) {
            val diaCalendar = dayMapping[diaNombre] ?: continue

            val candidateTime = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmaHora)
                set(Calendar.MINUTE, alarmaMinuto)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                var daysUntil = (diaCalendar - currentDayOfWeek + 7) % 7
                if (daysUntil == 0 && (get(Calendar.HOUR_OF_DAY) < currentTime.get(Calendar.HOUR_OF_DAY) ||
                            (get(Calendar.HOUR_OF_DAY) == currentTime.get(Calendar.HOUR_OF_DAY) && get(Calendar.MINUTE) <= currentTime.get(Calendar.MINUTE)))) {
                    add(Calendar.DAY_OF_YEAR, 7)
                } else {
                    add(Calendar.DAY_OF_YEAR, daysUntil)
                }
            }

            val timeDiff = candidateTime.timeInMillis - currentTime.timeInMillis

            if (timeDiff >= 0 && timeDiff < minTimeDiffMillis) {
                minTimeDiffMillis = timeDiff
                nextReminderDayName = diaNombre
                nextReminderTime = candidateTime
            }
        }

        return if (nextReminderTime != null) {
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(nextReminderTime.time)
            "Próximo: $nextReminderDayName a las $formattedTime"
        } else {
            "No hay recordatorios activos."
        }
    }
}