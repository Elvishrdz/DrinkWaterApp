package com.eahm.drinkwaterapp.system.broadcast

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.core.data.datastore.initialAmountPerDrink
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_CODE
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_NOTIFICATION_ID
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.system.service.goalcelebration.GoalCelebrationOverlay
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.extension.createIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AlarmReceiver : BroadcastReceiver() {

    private val drinkAlarmManager
        get() = GlobalContext.get().get<DrinkAlarmManager>()

    private val drinkAlarmManagerV2
        get() = GlobalContext.get().get<DrinkAlarmManagerV2>()

    override fun onReceive(context: Context, intent: Intent) = goAsync { _ ->
        Log.i("AlarmReceiverLogs", "onReceive: Intent:${intent.action}")
        val code = intent.getIntExtra(EXTRA_CODE, -1)

        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
                -> {
                println("DrinkWaterApp: restore alarms with received intent: ${intent.action}")
                // this is called multiple times depending on the intent action
                drinkAlarmManager.restoreAlarm(source = intent.action.toString())
                drinkAlarmManagerV2.restoreAlarm(source = intent.action.toString())
            }

            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                // Fired immediately when permission is granted, NOT when revoked (confirmed by testing).
                val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
                Log.i(
                    "AlarmReceiverLogs",
                    "SCHEDULE_EXACT_ALARM permission changed — granted=$granted"
                )
                if (granted) {
                    drinkAlarmManager.restoreAlarm(
                        source = AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
                    )
                    drinkAlarmManagerV2.restoreAlarm(
                        source = AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
                    )
                }
            }

            EXACT_ALARM_ACTION -> {
                val hour = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_HOUR, -1).takeIf { it != -1 }
                val minute = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_MINUTE, -1).takeIf { it != -1 }
                drinkAlarmManagerV2.onAlarmReceived(AlarmSource.DrinkAlarmReceived, hour, minute)
            }

            INEXACT_ALARM_ACTION -> {
                val hour = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_HOUR, -1).takeIf { it != -1 }
                val minute = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_MINUTE, -1).takeIf { it != -1 }
                drinkAlarmManagerV2.onInexactAlarmReceived(
                    AlarmSource.DrinkAlarmReceived,
                    hour,
                    minute
                )
            }

            ALARM_ACTION -> {
                val hour = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_HOUR, -1).takeIf { it != -1 }
                val minute = intent.getIntExtra(ALARM_EXTRAS_TRIGGER_MINUTE, -1).takeIf { it != -1 }

                drinkAlarmManager.onAlarmReceived(AlarmSource.DrinkAlarmReceived)

                NotificationService.showDrinkNotification(context, hour, minute)
            }

            Drink.action -> {
                if (code == Drink.code) {
                    val amountPerDrink = intent.getFloatExtra(
                        DrinkNotification.AMOUNT_PER_DRINK_ML,
                        initialAmountPerDrink
                    )
                    registerDrink(context, amountPerDrink)
                    drinkAlarmManager.setupAlarm(AlarmSource.NotificationDrinkButton)
                    removeNotificationFromSystem(context, intent)
                }
            }

            SkipDrink.action -> {
                if (code == SkipDrink.code) {
                    drinkAlarmManager.setupAlarm(AlarmSource.NotificationSkipButton)
                    removeNotificationFromSystem(context, intent)
                }
            }

            NotificationRemoved.action -> {
                if (code == NotificationRemoved.code) {
                    drinkAlarmManager.setupAlarm(AlarmSource.NotificationRemoved)
                    removeNotificationFromSystem(context, intent)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun registerDrink(context: Context, amountPerDrink: Float) =
        withContext(Dispatchers.IO) {
            val koin = GlobalContext.get()
            val database = koin.get<AppDatabase>()
            val dao = database.getHistoryDao()
            val drinkStatsObserver = koin.get<DrinkStatsObserver>()

            // Snapshot goal state before the insert to detect the not-reached → reached transition
            val statsBefore = drinkStatsObserver.drinkStats.first()

            val record = HistoryEntity(
                amount = amountPerDrink,
                status = Status.FROM_NOTIFICATION,
            )
            dao.insertAll(record)

            // Show celebration overlay only when this drink crosses the goal threshold
            val goalJustReached = !statsBefore.goalReached &&
                    (statsBefore.drinkAmount + amountPerDrink) >= statsBefore.goalAmount

            if (goalJustReached && Settings.canDrawOverlays(context)) {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val drinkCount = dao.findByTimestamp(
                    start = today.toStartOfDayTime,
                    end = today.toEndOfDayTime,
                ).first().size

                val intent = GoalCelebrationOverlay.createIntent(
                    context = context,
                    style = CelebrationStyle.Ocean,
                    goalMl = statsBefore.goalAmount,
                    drinkCount = drinkCount.coerceAtLeast(1),
                    streakDays = 0,
                )

                context.startService(intent)
            }
        }

    private fun removeNotificationFromSystem(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object {

        // Actions must match the intent-filter entries in AndroidManifest.xml
        const val ALARM_ACTION = "com.eahm.drinkwaterapp.ALARM_ACTION"
        const val EXACT_ALARM_ACTION = "com.eahm.drinkwaterapp.EXACT_ALARM_ACTION"
        const val INEXACT_ALARM_ACTION = "com.eahm.drinkwaterapp.INEXACT_ALARM_ACTION"

        const val ALARM_EXTRAS_TRIGGER_HOUR = "EXTRAS_TRIGGER_HOUR"
        const val ALARM_EXTRAS_TRIGGER_MINUTE = "EXTRAS_TRIGGER_MINUTE"
    }
}
