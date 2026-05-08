package com.eahm.drinkwaterapp.system.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_CODE
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_NOTIFICATION_ID
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

class AlarmReceiver : BroadcastReceiver() {

    private val drinkAlarmManager
        get() = let {
            val koin = GlobalContext.get()
            koin.get<DrinkAlarmManager>()
        }

    override fun onReceive(context: Context, intent: Intent) = goAsync { _ ->
        val code = intent.getIntExtra(EXTRA_CODE, -1)

        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
                -> {
                println("DrinkWaterApp: restore alarms with received intent: ${intent.action}")
                // this is called multiple times depending on the intent action
                drinkAlarmManager.restoreAlarm(source = intent.action.toString())
            }

            ALARM_ACTION -> {
                drinkAlarmManager.onAlarmReceived(AlarmSource.DrinkAlarmReceived)

                NotificationService.showDrinkNotification(context)
            }

            Drink.action -> {
                if (code == Drink.code) {
                    registerDrink()
                    drinkAlarmManager.determineAction(AlarmSource.NotificationDrinkButton)
                    removeNotificationFromSystem(context, intent)
                }
            }

            SkipDrink.action -> {
                if (code == SkipDrink.code) {
                    drinkAlarmManager.determineAction(AlarmSource.NotificationSkipButton)
                    removeNotificationFromSystem(context, intent)
                }
            }

            NotificationRemoved.action -> {
                if (code == NotificationRemoved.code) {
                    drinkAlarmManager.determineAction(AlarmSource.NotificationRemoved)
                    removeNotificationFromSystem(context, intent)
                }
            }
        }
    }

    private suspend fun registerDrink() = withContext(Dispatchers.IO) {
        val koin = GlobalContext.get()
        val database = koin.get<AppDatabase>()
        val dao = database.getHistoryDao()

        // TODO: get notification setup amount from shared prefs
        val record = HistoryEntity(
            amount = 150f,
            status = Status.FROM_NOTIFICATION,
        )

        dao.insertAll(record)
    }

    private fun removeNotificationFromSystem(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object {

        // Action should match action in the AndroidManifest.xml receiver intent-filter
        const val ALARM_ACTION = "com.eahm.drinkwaterapp.ALARM_ACTION"
    }
}
