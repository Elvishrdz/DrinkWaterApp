package com.eahm.drinkwaterapp.system.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import com.eahm.drinkwaterapp.system.foreground.AppVisibility

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
                -> {
                restoreAlarmsFromStorage(context)
            }

            ALARM_ACTION -> {
                val alarmId = intent.getIntExtra("alarm_id", -1)
                val intervalMs = intent.getLongExtra("interval_ms", 60_000L)

                val isAppVisible = AppVisibility.isAppInForeground.value

                if (isAppVisible) {
                    // if app is in foreground, do not show notifications (remove existing)
//                    val extras = intent.extras
//                    val notificationID: Int = extras?.getInt("NOTI_ID") ?: -1
//                    val notificationManager = NotificationManagerCompat.from(context)
//                    notificationManager.cancel(notificationID)
                } else {
                    NotificationService.showDrinkAlarmNotification(context)
                }

                // 2. Auto-renovar: programar la siguiente
                prepareNextAlarm(context, alarmId, intervalMs)
            }

            Drink.action -> {

            }

            SkipDrink.action -> {

            }

            NotificationRemoved.action -> {

            }
        }
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
