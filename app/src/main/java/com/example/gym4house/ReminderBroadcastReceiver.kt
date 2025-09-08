package com.example.gym4house

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log // Importar Log
import android.Manifest // Importar Manifest para SecurityException
import android.content.pm.PackageManager // Importar PackageManager


class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "gym4house_reminders"
        const val NOTIFICATION_ID_BASE = 100
        const val REMINDER_MESSAGE_EXTRA = "reminder_message"
        private const val TAG = "ReminderReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        Log.d(TAG, "¡Broadcast recibido en ReminderBroadcastReceiver! Acción: ${intent.action}")

        // Re-schedule alarms after boot
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "ACTION_BOOT_COMPLETED recibido. Intentando reprogramar recordatorios.")
            // Aquí, en un escenario real, cargarías las preferencias de todos los usuarios
            // y programarías sus recordatorios. Por ahora, solo logeamos.
            val reminderScheduler = ReminderScheduler(context)
            reminderScheduler.reScheduleRemindersAfterBoot()
            return // No procesar como una notificación de recordatorio regular
        }

        if (intent.action == "com.example.gym4house.ACTION_REMINDER") {
            Log.d(TAG, "Acción de recordatorio detectada. Intentando mostrar notificación.")

            val message = intent.getStringExtra(REMINDER_MESSAGE_EXTRA) ?: "¡Es hora de tu entrenamiento!"

            // Para Android 13+ (API 33), necesitamos verificar si el permiso POST_NOTIFICATIONS está concedido
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "ERROR: Permiso POST_NOTIFICATIONS no concedido. No se puede mostrar la notificación.")
                    return // No se puede mostrar la notificación sin el permiso
                }
            }

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener un icono ic_notification en tus drawables
                .setContentTitle("Recordatorio de Entrenamiento")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationId = intent.getIntExtra("request_code", NOTIFICATION_ID_BASE) // Usar un ID único por recordatorio
            Log.d(TAG, "Mostrando notificación con ID: $notificationId y mensaje: $message")

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
                Log.d(TAG, "Notificación mostrada con éxito (o al menos la llamada a notify() se realizó).")
            } catch (e: SecurityException) {
                // Esto ocurre si el permiso POST_NOTIFICATIONS no está concedido en versiones anteriores a T
                Log.e(TAG, "ERROR: SecurityException al mostrar notificación. Permiso no concedido o problema en manifiesto.", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al mostrar la notificación: ${e.message}", e)
            }
        }
    }
}