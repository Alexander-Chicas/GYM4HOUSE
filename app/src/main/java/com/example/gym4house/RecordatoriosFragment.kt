package com.example.gym4house

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import android.provider.Settings // Importar
import android.net.Uri // Importar
import android.util.Log // Importar para logs de depuración

class RecordatoriosFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var alarmManager: AlarmManager

    private lateinit var textViewSelectedTime: TextView
    private lateinit var buttonGuardarRecordatorio: Button

    // Checkboxes para los días
    private lateinit var checkBoxLunes: CheckBox
    private lateinit var checkBoxMartes: CheckBox
    private lateinit var checkBoxMiercoles: CheckBox
    private lateinit var checkBoxJueves: CheckBox
    private lateinit var checkBoxViernes: CheckBox
    private lateinit var checkBoxSabado: CheckBox
    private lateinit var checkBoxDomingo: CheckBox

    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1

    // Constante para el TAG de logs
    private val TAG = "RecordatoriosFragment"

    // Request Launcher para permisos de notificación (POST_NOTIFICATIONS)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Permiso POST_NOTIFICATIONS concedido.")
        } else {
            Toast.makeText(context, "Permiso de notificaciones denegado. Los recordatorios podrían no mostrarse.", Toast.LENGTH_LONG).show()
            Log.w(TAG, "Permiso POST_NOTIFICATIONS denegado.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d(TAG, "RecordatoriosFragment onCreate - AlarmManager inicializado.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recordatorios, container, false)

        textViewSelectedTime = view.findViewById(R.id.textViewSelectedTime)
        buttonGuardarRecordatorio = view.findViewById(R.id.buttonGuardarRecordatorio)

        checkBoxLunes = view.findViewById(R.id.checkBoxLunes)
        checkBoxMartes = view.findViewById(R.id.checkBoxMartes)
        checkBoxMiercoles = view.findViewById(R.id.checkBoxMiercoles)
        checkBoxJueves = view.findViewById(R.id.checkBoxJueves)
        checkBoxViernes = view.findViewById(R.id.checkBoxViernes)
        checkBoxSabado = view.findViewById(R.id.checkBoxSabado)
        checkBoxDomingo = view.findViewById(R.id.checkBoxDomingo)

        textViewSelectedTime.setOnClickListener {
            showTimePickerDialog()
        }

        buttonGuardarRecordatorio.setOnClickListener {
            saveReminderSettings()
        }

        loadReminderSettings()
        requestNotificationPermission() // Solicita POST_NOTIFICATIONS (para Android 13+)
        checkAndRequestExactAlarmPermission() // Solicita permiso para alarmas exactas (para Android 12+)

        Log.d(TAG, "RecordatoriosFragment onCreateView - UI configurada.")
        return view
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        // Usar la hora y minuto seleccionados si ya existen, de lo contrario, la hora actual
        val hour = selectedHour.takeIf { it != -1 } ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedMinute.takeIf { it != -1 } ?: calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, h: Int, m: Int ->
                selectedHour = h
                selectedMinute = m
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                textViewSelectedTime.text = formattedTime
                Log.d(TAG, "Hora seleccionada: $formattedTime")
            },
            hour,
            minute,
            true // Formato de 24 horas
        )
        timePickerDialog.show()
    }

    private fun saveReminderSettings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Intento de guardar recordatorio sin usuario autenticado.")
            return
        }

        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(context, "Por favor, selecciona una hora para el recordatorio.", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Intento de guardar recordatorio sin hora seleccionada.")
            return
        }

        val selectedDays = mutableListOf<String>()
        if (checkBoxLunes.isChecked) selectedDays.add("Lunes")
        if (checkBoxMartes.isChecked) selectedDays.add("Martes")
        if (checkBoxMiercoles.isChecked) selectedDays.add("Miércoles")
        if (checkBoxJueves.isChecked) selectedDays.add("Jueves")
        if (checkBoxViernes.isChecked) selectedDays.add("Viernes")
        if (checkBoxSabado.isChecked) selectedDays.add("Sábado")
        if (checkBoxDomingo.isChecked) selectedDays.add("Domingo")

        if (selectedDays.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona al menos un día para el recordatorio.", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Intento de guardar recordatorio sin días seleccionados.")
            return
        }

        // MUY IMPORTANTE: Antes de intentar programar la alarma, verifica el permiso de alarmas exactas.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) o superior
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Para programar recordatorios, por favor, concede el permiso de 'Alarmas y recordatorios' en Ajustes de la aplicación.", Toast.LENGTH_LONG).show()
                checkAndRequestExactAlarmPermission() // Intentar redirigir al usuario
                Log.w(TAG, "Permiso de alarmas exactas denegado. No se pueden guardar/programar recordatorios.")
                return // Detener el proceso de guardado/programación aquí
            }
        }

        val reminderData = hashMapOf(
            "horaRecordatorio" to String.format("%02d:%02d", selectedHour, selectedMinute),
            "diasRecordatorio" to selectedDays
        )

        firestore.collection("usuarios").document(userId)
            .update(reminderData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Recordatorio guardado correctamente.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Recordatorio guardado en Firestore. Procediendo a programar alarmas.")
                // Una vez guardado en Firestore, programar las alarmas
                scheduleReminders(selectedDays)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al guardar recordatorio: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error al guardar recordatorio en Firestore: ${e.message}", e)
            }
    }

    private fun loadReminderSettings() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "No hay usuario autenticado para cargar recordatorios.")
            return
        }

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val hora = document.getString("horaRecordatorio")
                    val dias = document.get("diasRecordatorio") as? List<String>

                    if (hora != null) {
                        textViewSelectedTime.text = hora
                        val parts = hora.split(":")
                        if (parts.size == 2) {
                            selectedHour = parts[0].toIntOrNull() ?: -1
                            selectedMinute = parts[1].toIntOrNull() ?: -1
                        }
                        Log.d(TAG, "Recordatorio cargado: Hora=$hora")
                    }

                    if (dias != null) {
                        checkBoxLunes.isChecked = dias.contains("Lunes")
                        checkBoxMartes.isChecked = dias.contains("Martes")
                        checkBoxMiercoles.isChecked = dias.contains("Miércoles")
                        checkBoxJueves.isChecked = dias.contains("Jueves")
                        checkBoxViernes.isChecked = dias.contains("Viernes")
                        checkBoxSabado.isChecked = dias.contains("Sábado")
                        checkBoxDomingo.isChecked = dias.contains("Domingo")
                        Log.d(TAG, "Recordatorio cargado: Días=${dias.joinToString()}")
                    }
                } else {
                    Log.d(TAG, "No se encontraron recordatorios guardados para el usuario.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cargar recordatorios desde Firestore: ${e.message}")
            }
    }

    // Función para solicitar el permiso de notificación (POST_NOTIFICATIONS)
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 = TIRAMISU
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Permiso POST_NOTIFICATIONS ya concedido.")
                // Permiso ya concedido
            } else {
                Log.d(TAG, "Solicitando permiso POST_NOTIFICATIONS.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            Log.d(TAG, "Dispositivo con API < 33. No se requiere POST_NOTIFICATIONS.")
        }
    }

    // NUEVA FUNCIÓN: Verificar y solicitar permiso para programar alarmas exactas
    private fun checkAndRequestExactAlarmPermission() {
        // Para Android 12 (API 31) y superiores, se necesita un permiso especial
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // El permiso no ha sido concedido por el usuario
                Toast.makeText(context, "Se necesita permiso para programar alarmas exactas. Redirigiendo a Ajustes.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                Log.w(TAG, "Permiso SCHEDULE_EXACT_ALARM denegado. Redirigiendo a Ajustes.")
                startActivity(intent)
            } else {
                Log.d(TAG, "Permiso SCHEDULE_EXACT_ALARM ya concedido.")
            }
        } else {
            Log.d(TAG, "Dispositivo con API < 31. No se requiere SCHEDULE_EXACT_ALARM (implícito).")
        }
    }


    private fun scheduleReminders(days: List<String>) {
        Log.d(TAG, "Iniciando scheduleReminders para días: ${days.joinToString()}")
        // Primero, cancelar todas las alarmas existentes para este usuario (para evitar duplicados al guardar)
        cancelAllReminders()

        // Si el usuario no ha seleccionado una hora válida, no programar
        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(context, "Hora de recordatorio no seleccionada, no se pueden programar alarmas.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "selectedHour o selectedMinute son -1. No se pueden programar alarmas.")
            return
        }

        // Verificar el permiso de alarmas exactas justo antes de programar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "No se pudieron programar los recordatorios: permiso de 'Alarmas y recordatorios' denegado.", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Permiso SCHEDULE_EXACT_ALARM denegado antes de programar. Abortando programación.")
                return
            }
        }

        days.forEach { dayName ->
            val dayOfWeek = getDayOfWeek(dayName)

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Ajustar la fecha para que sea el próximo día de la semana deseado.
            // Si el día actual es el mismo que el deseado pero la hora ya pasó, avanza una semana.
            // Si el día actual no es el mismo que el deseado, encuentra el próximo día.
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (currentDayOfWeek == dayOfWeek) {
                // Si es hoy y la hora ya pasó, programar para la próxima semana.
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    Log.d(TAG, "La hora para $dayName ya pasó hoy. Ajustando a la próxima semana.")
                }
            } else {
                // Si no es hoy, ajustar al próximo día de la semana.
                // Calcula cuántos días hay hasta el día de la semana deseado
                // (+7)%7 asegura que el resultado sea siempre positivo para el cálculo de días futuros
                val daysUntilNext = (dayOfWeek - currentDayOfWeek + 7) % 7
                calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
                Log.d(TAG, "Ajustando fecha para $dayName. Días hasta el próximo: $daysUntilNext")
            }


            // Añadir un log para verificar el tiempo programado para la primera activación
            val programmedTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            val programmedDate = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            Log.d(TAG, "Programando alarma para $dayName ($programmedDate) a las $programmedTime (Millis: ${calendar.timeInMillis})")


            val requestCode = getRequestCodeForDay(dayOfWeek) // RequestCode único por día para cada alarma repetitiva

            val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                action = "com.example.gym4house.ACTION_REMINDER"
                // Añadir datos únicos si es necesario, aunque el action ya lo es para tu BroadcastReceiver.
                // Es buena práctica añadir un ID único para la notificación si tuvieras múltiples tipos.
                putExtra(ReminderBroadcastReceiver.REMINDER_MESSAGE_EXTRA, "¡Es hora de tu entrenamiento diario para $dayName!")
                putExtra("request_code", requestCode) // Pasar el request_code a la notificación si lo usas.
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode, // Usa el request code específico para cada día
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE es obligatorio para Android 12+
            )

            // Usar setRepeating para alarmas que se repiten semanalmente.
            // RTC_WAKEUP despierta el dispositivo si está en modo Doze.
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis, // Primera activación
                AlarmManager.INTERVAL_DAY * 7, // Repetir cada 7 días (semanalmente)
                pendingIntent
            )
            Toast.makeText(context, "Recordatorio programado para $dayName a las ${String.format("%02d:%02d", selectedHour, selectedMinute)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelAllReminders() {
        Log.d(TAG, "Cancelando todas las alarmas existentes.")
        val daysOfWeek = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )

        daysOfWeek.forEach { dayOfWeek ->
            val requestCode = getRequestCodeForDay(dayOfWeek)
            val reminderIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                action = "com.example.gym4house.ACTION_REMINDER"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                reminderIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Usar FLAG_NO_CREATE para solo obtener si ya existe
            )

            // Si pendingIntent no es nulo, significa que existía una alarma y podemos cancelarla
            if (pendingIntent != null) {
                pendingIntent.cancel() // Cancelar el PendingIntent
                alarmManager.cancel(pendingIntent) // Asegurarse de cancelar la alarma en AlarmManager
                Log.d(TAG, "Alarma cancelada para el díaOfWeek: $dayOfWeek (RequestCode: $requestCode)")
            } else {
                Log.d(TAG, "No se encontró alarma para cancelar el díaOfWeek: $dayOfWeek (RequestCode: $requestCode)")
            }
        }
        // No Toast aquí para evitar muchos mensajes al cancelar
    }

    private fun getDayOfWeek(dayName: String): Int {
        return when (dayName) {
            "Lunes" -> Calendar.MONDAY
            "Martes" -> Calendar.TUESDAY
            "Miércoles" -> Calendar.WEDNESDAY
            "Jueves" -> Calendar.THURSDAY
            "Viernes" -> Calendar.FRIDAY
            "Sábado" -> Calendar.SATURDAY
            "Domingo" -> Calendar.SUNDAY
            else -> {
                Log.e(TAG, "Día de la semana inválido proporcionado: $dayName")
                throw IllegalArgumentException("Día de la semana inválido: $dayName")
            }
        }
    }

    // Usaremos una base para los request codes para que sean únicos y fáciles de identificar
    // y cancelar por día.
    private fun getRequestCodeForDay(dayOfWeek: Int): Int {
        // Por ejemplo, NOTIFICATION_ID_BASE + Calendar.MONDAY (2), NOTIFICATION_ID_BASE + Calendar.TUESDAY (3), etc.
        return ReminderBroadcastReceiver.NOTIFICATION_ID_BASE + dayOfWeek
    }
}