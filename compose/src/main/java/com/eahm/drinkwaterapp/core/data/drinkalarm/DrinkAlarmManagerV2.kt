package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.util.Log
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2.Companion.EXACT_ALARM_ID
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2.Companion.INEXACT_ALARM_ID
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

/**
 * Alarm manager for the dual-alarm system.
 *
 * Runs two independent alarms in parallel:
 *  - Exact alarm  (ID [EXACT_ALARM_ID]):   primary, fires notifications when permission is granted.
 *  - Inexact alarm (ID [INEXACT_ALARM_ID]): guard, always alive, acts as fallback when exact is dead.
 *
 * All scheduling paths share [schedulingMutex] to prevent race conditions when both alarms
 * fire simultaneously.
 *
 * NOTE: Uses hardcoded test data (slots 10:00 / 13:00 / 16:00, active hours 06:00–18:00).
 * Replace with PrefsStorage reads when the testing phase is complete.
 */
class DrinkAlarmManagerV2(
    private val application: Application,
) {

    private val schedulingMutex = Mutex()

    // region Hardcoded test data

    /** Fixed reminder slots used during the testing phase. */
    private val slots = listOf(
        Pair(10, 0),
        Pair(13, 0),
        Pair(16, 0),
    )

    private val activeStartHour = 6
    private val activeEndHour = 18

    // endregion

    // region Public API

    /**
     * Called on boot, package replaced, and permission re-granted.
     * Schedules both alarms if within active hours and either alarm is missing or stale.
     */
    suspend fun restoreAlarm(source: String = "unknown") {
        Log.i(TAG, "restoreAlarm: source=$source — waiting for lock")
        schedulingMutex.withLock {
            Log.i(TAG, "restoreAlarm: source=$source — lock acquired")
            if (!isWithinActiveHours()) {
                Log.i(
                    TAG,
                    "restoreAlarm: outside active hours ($activeStartHour:00–$activeEndHour:00) — skipping"
                )
                return@withLock
            }
            scheduleNextAlarm()
        }
    }

    /**
     * Called when the exact alarm ([EXACT_ALARM_ACTION]) fires.
     * Reschedules both alarms and shows the notification — only if exact permission is still granted.
     */
    suspend fun onAlarmReceived(source: AlarmSource, hour: Int?, minute: Int?) {
        schedulingMutex.withLock {
            Log.i(TAG, "onAlarmReceived (exact): source=$source")

            if (!ExactAlarmPermission.isGranted(application)) {
                // Theoretically unreachable: Android cancels exact alarms when permission is
                // revoked. Handled here as a safety net — the inexact alarm will take over.
                Log.i(TAG, "onAlarmReceived: exact permission not granted — skipping")
                return@withLock
            }

            scheduleNextAlarm()
            NotificationService.showDrinkNotification(application, hour, minute)
        }
    }

    /**
     * Called when the inexact guard alarm ([INEXACT_ALARM_ACTION]) fires.
     *
     * Decision tree:
     *  - Exact permission granted + exact alarm active  → sync inexact to next slot, no notification
     *  - Exact permission granted + exact alarm missing → restore both alarms, no notification
     *  - Exact permission revoked                       → fallback: show notification, reschedule inexact
     */
    suspend fun onInexactAlarmReceived(source: AlarmSource, hour: Int?, minute: Int?) {
        schedulingMutex.withLock {
            val exactPermissionGranted = ExactAlarmPermission.isGranted(application)
            val exactAlarmActive = AlarmSchedulerV3.isExactAlarmActive(application, EXACT_ALARM_ID)

            Log.i(
                TAG,
                "onInexactAlarmReceived: source=$source " +
                        "exactPermission=$exactPermissionGranted exactActive=$exactAlarmActive",
            )

            when {
                exactPermissionGranted && exactAlarmActive -> {
                    // Normal state — exact alarm is alive and will show the notification.
                    // Keep the inexact in sync with the same next slot.
                    Log.i(
                        TAG,
                        "onInexactAlarmReceived: exact alarm active — syncing inexact, no notification"
                    )
                    val (nextHour, nextMinute) = getNextSlot()
                    AlarmSchedulerV3.scheduleInexactAlarm(
                        application,
                        INEXACT_ALARM_ID,
                        nextHour,
                        nextMinute,
                    )
                }

                exactPermissionGranted && !exactAlarmActive -> {
                    // Sync needed — permission is granted but the exact alarm was somehow lost.
                    // Restore both alarms silently (next exact fire will show the notification).
                    Log.i(
                        TAG,
                        "onInexactAlarmReceived: exact alarm missing — restoring both, no notification"
                    )
                    scheduleNextAlarm()
                }

                else -> {
                    // Fallback — exact permission is revoked; inexact acts as the active alarm.
                    // scheduleNextAlarm() only schedules the inexact here (exact skipped, no permission).
                    Log.i(
                        TAG,
                        "onInexactAlarmReceived: no exact permission — fallback notification"
                    )
                    scheduleNextAlarm()
                    NotificationService.showDrinkNotification(application, hour, minute)
                }
            }
        }
    }

    fun stopAlarm() {
        Log.i(TAG, "stopAlarm: cancelling both alarms")
        AlarmSchedulerV3.cancelAllAlarms(application, EXACT_ALARM_ID, INEXACT_ALARM_ID)
    }

    fun clear() {
        stopAlarm()
        NotificationService.cancelDrinkNotification(application)
    }

    // endregion

    // region Private helpers

    /**
     * Schedules the exact alarm (if permission is granted) and the inexact alarm
     * for the next available slot.
     */
    private fun scheduleNextAlarm() {
        val (hour, minute) = getNextSlot()
        Log.i(TAG, "scheduleNextAlarm: next slot at $hour:$minute")

        if (ExactAlarmPermission.isGranted(application)) {
            AlarmSchedulerV3.scheduleExactAlarm(application, EXACT_ALARM_ID, hour, minute)
        }
        AlarmSchedulerV3.scheduleInexactAlarm(application, INEXACT_ALARM_ID, hour, minute)
    }

    /**
     * Returns the first slot from [slots] that is still in the future today.
     * Falls back to the first slot (scheduled for the next day) if all have passed.
     */
    private fun getNextSlot(): Pair<Int, Int> {
        val now = Calendar.getInstance()
        val nowHour = now.get(Calendar.HOUR_OF_DAY)
        val nowMinute = now.get(Calendar.MINUTE)

        for ((hour, minute) in slots) {
            if (hour > nowHour || (hour == nowHour && minute > nowMinute)) {
                return Pair(hour, minute)
            }
        }

        // All slots have passed today — schedule for the first slot tomorrow.
        // AlarmSchedulerV3.delayUntilNext() handles the day rollover automatically.
        return slots.first()
    }

    private fun isWithinActiveHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in activeStartHour until activeEndHour
    }

    // endregion

    companion object {
        /** Request code for the exact alarm PendingIntent. */
        const val EXACT_ALARM_ID = 127

        /** Request code for the inexact guard alarm PendingIntent. */
        const val INEXACT_ALARM_ID = 128

        private const val TAG = "DrinkAlarmManagerV2"
    }
}
