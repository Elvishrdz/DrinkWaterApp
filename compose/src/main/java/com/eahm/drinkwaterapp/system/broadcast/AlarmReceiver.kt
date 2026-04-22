package com.eahm.drinkwaterapp.system.broadcast

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED" -> {
                restoreAlarmsFromStorage(context)
            }

            ALARM_ACTION -> {
                val alarmId = intent.getIntExtra("alarm_id", -1)
                val intervalMs = intent.getLongExtra("interval_ms", 60_000L)

                // 1. Mostrar notificación
                showNotification(context, alarmId)

                // 2. Auto-renovar: programar la siguiente
                prepareNextAlarm(context, alarmId, intervalMs)
            }
        }
    }

    private fun showNotification(context: Context, alarmId: Int) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            PackageManager.PERMISSION_GRANTED // for VERSION.SDK_INT < TIRAMISU
        }

        if (permission != PackageManager.PERMISSION_GRANTED) return


        val channelId = "alarm_channel"

        // Crear canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle("Alarma #$alarmId")
            .setContentText("Es hora!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(alarmId, notification)
    }

    private fun restoreAlarmsFromStorage(context: Context) {
        val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val alarmIds = prefs.getStringSet("active_alarms", emptySet()) ?: return

        alarmIds.forEach { idStr ->
            val id = idStr.toIntOrNull() ?: return@forEach
            val intervalMs = prefs.getLong("alarm_interval_$id", 60_000L)
            AlarmScheduler.scheduleAlarm(context, id, intervalMs)
        }
    }

    private fun prepareNextAlarm(
        context: Context,
        alarmId: Int,
        intervalMs: Long
    ) {
        AlarmScheduler.scheduleAlarm(context, alarmId, intervalMs)
    }

    companion object {

        // Action should match action in the AndroidManifest.xml receiver intent-filter
        const val ALARM_ACTION = "com.eahm.drinkwaterapp.ALARM_ACTION"
    }
}
