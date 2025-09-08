package com.example.gym4house

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val REMINDER_REQUEST_CODE = 1001 // Un código único para este PendingIntent
        private const val TAG = "ReminderScheduler"
    }

    /**
     * Crea el canal de notificación para Android 8.0 (API 26) y superior.
     * Es crucial llamarlo antes de intentar mostrar cualquier notificación.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name) // Define this in strings.xml
            val descriptionText = context.getString(R.string.channel_description) // Define this in strings.xml
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(ReminderBroadcastReceiver.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: ${ReminderBroadcastReceiver.NOTIFICATION_CHANNEL_ID}")
        }
    }

    /**
     * Programa un recordatorio diario a la hora y minuto especificados.
     */
    fun scheduleReminder(hour: Int, minute: Int, frequency: String) {
        // Cancelar cualquier recordatorio existente para evitar duplicados
        cancelReminder()

        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si la hora programada ya pasó para hoy, programa para mañana
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = "com.example.gym4house.ACTION_REMINDER"
            putExtra(ReminderBroadcastReceiver.REMINDER_MESSAGE_EXTRA, "¡Es hora de tu entrenamiento!") // Mensaje por defecto
            putExtra("request_code", REMINDER_REQUEST_CODE) // Usamos el request code para identificar esta alarma
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE, // Usa el request code para que sea único para esta alarma
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val intervalMillis = AlarmManager.INTERVAL_DAY // Recordatorio diario

        // Usar RTC_WAKEUP para que la alarma se dispare incluso si el dispositivo está en modo "dormido"
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            intervalMillis,
            pendingIntent
        )
        Log.d(TAG, "Reminder scheduled for ${String.format("%02d:%02d", hour, minute)} daily. First trigger: ${calendar.time}")
    }

    /**
     * Cancela cualquier recordatorio pendiente programado por este scheduler.
     */
    fun cancelReminder() {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = "com.example.gym4house.ACTION_REMINDER"
            // Asegurarse de que el Intent sea idéntico al que se usó para programar la alarma
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0 // FLAG_IMMUTABLE es necesario para la cancelación
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // Liberar el PendingIntent
        Log.d(TAG, "Any existing reminder cancelled.")
    }

    /**
     * Esta función sería llamada después de un reinicio del dispositivo.
     * En un escenario real, aquí cargarías las preferencias de recordatorio
     * del usuario (desde Firestore o SharedPreferences) y reprogramarías
     * las alarmas. Por simplicidad, por ahora solo registraremos un log.
     */
    fun reScheduleRemindersAfterBoot() {
        Log.d(TAG, "Re-scheduling reminders after boot. (Implementation pending: load user settings and schedule)")
        // TODO: Implementar la lógica para cargar la configuración del usuario y reprogramar recordatorios
        // Esto requeriría acceder a Firebase/SharedPreferences para cada usuario o el usuario activo.
        // Por ahora, el ReminderBroadcastReceiver solo registra que recibió el BOOT_COMPLETED.
    }
}
