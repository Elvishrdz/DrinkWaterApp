package com.eahm.drinkwaterapp.system.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_CODE
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_NOTIFICATION_ID
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import org.koin.core.context.GlobalContext

class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getIntExtra(EXTRA_CODE, -1)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
                -> {
                restoreAlarmsFromStorage(context)
            }

            ALARM_ACTION -> {
                NotificationService.showDrinkNotification(context)
            }

            Drink.action -> {
                if (code == Drink.code) {
                    // TODO: save new drink to database
                    scheduleNextAlarm(context)
                    removeNotificationFromSystem(context, intent)
                }
            }

            SkipDrink.action -> {
                if (code == SkipDrink.code) {
                    scheduleNextAlarm(context)
                    removeNotificationFromSystem(context, intent)
                }
            }

            NotificationRemoved.action -> {
                if (code == NotificationRemoved.code) {
                    scheduleNextAlarm(context)
                    removeNotificationFromSystem(context, intent)
                }
            }
        }
    }

    private fun scheduleNextAlarm(context: Context) {
        val koin = GlobalContext.get()
        val drinkAlarmManager = koin.get<DrinkAlarmManager>()
        drinkAlarmManager.setupAlarm(context)
    }

    private fun removeNotificationFromSystem(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        NotificationManagerCompat.from(context).cancel(notificationId)
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

    companion object {

        // Action should match action in the AndroidManifest.xml receiver intent-filter
        const val ALARM_ACTION = "com.eahm.drinkwaterapp.ALARM_ACTION"
    }
}
