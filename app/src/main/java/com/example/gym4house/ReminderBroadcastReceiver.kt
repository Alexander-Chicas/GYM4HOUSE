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

        if (intent.action == "com.example.gym4house.ACTION_REMINDER") {
            Log.d(TAG, "Acción de recordatorio detectada. Intentando mostrar notificación.")

            val message = intent.getStringExtra(REMINDER_MESSAGE_EXTRA) ?: "¡Es hora de tu entrenamiento!"

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Recordatorio de Entrenamiento")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationId = intent.getIntExtra("request_code", NOTIFICATION_ID_BASE)
            Log.d(TAG, "Mostrando notificación con ID: $notificationId y mensaje: $message")

            // --- INICIO DEL CAMBIO PARA MANEJAR LA EXCEPCIÓN ---
            try {
                // Muestra la notificación
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
                Log.d(TAG, "Notificación mostrada con éxito (o al menos la llamada a notify() se realizó).")
            } catch (e: SecurityException) {
                // Esto ocurre si el permiso POST_NOTIFICATIONS no está concedido
                Log.e(TAG, "ERROR: Permiso POST_NOTIFICATIONS no concedido. No se puede mostrar la notificación.", e)
                // Aquí podrías guardar un log interno o hacer algo para informar al usuario si es necesario
            } catch (e: Exception) {
                // Captura cualquier otra excepción inesperada al mostrar la notificación
                Log.e(TAG, "Error inesperado al mostrar la notificación: ${e.message}", e)
            }
            // --- FIN DEL CAMBIO ---
        }
    }
}