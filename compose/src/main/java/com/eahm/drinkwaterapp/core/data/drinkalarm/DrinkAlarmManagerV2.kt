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
        Pair(14, 29),
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
            val result = scheduleNextAlarm()
            Log.i(TAG, "restoreAlarm: source=$source — $result")
        }
    }

    /**
     * Called when the exact alarm (EXACT_ALARM_ACTION) fires.
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

            val result = scheduleNextAlarm()
            Log.i(TAG, "onAlarmReceived: $result")
            NotificationService.showDrinkNotification(application, hour, minute)
        }
    }

    /**
     * Called when the inexact guard alarm (INEXACT_ALARM_ACTION) fires.
     *
     * Decision tree:
     *  - Exact permission granted + exact alarm active  → sync inexact to next slot, no notification
     *  - Exact permission granted + exact alarm missing → restore both alarms, no notification
     *  - Exact permission revoked                       → fallback: show notification, reschedule inexact
     */
    suspend fun onInexactAlarmReceived(source: AlarmSource, hour: Int?, minute: Int?) {
        schedulingMutex.withLock {
            // Always clear the previous receipt first, then save a new one only if this
            // alarm fired before its scheduled time (inexact alarms can be batched early).
            AlarmSchedulerV3.clearInexactEarlyReceipt(application, INEXACT_ALARM_ID)
            if (hour != null && minute != null) {
                val now = Calendar.getInstance()
                val scheduled = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
                }
                val earlyMs = scheduled.timeInMillis - now.timeInMillis
                if (earlyMs > 0) {
                    AlarmSchedulerV3.saveInexactEarlyReceipt(application, INEXACT_ALARM_ID, earlyMs, hour, minute)
                    Log.i(TAG, "onInexactAlarmReceived: fired ${earlyMs}ms early (scheduled at %02d:%02d)".format(hour, minute))
                }
            }

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
                    // Keep the inexact in sync with the same next slot only if it has drifted.
                    val (nextHour, nextMinute) = getNextSlot()
                    val nextSlot = Pair(nextHour, nextMinute)
                    val inexactActive =
                        AlarmSchedulerV3.isInexactAlarmActive(application, INEXACT_ALARM_ID)
                    val storedInexact =
                        AlarmSchedulerV3.getStoredInexactSlot(application, INEXACT_ALARM_ID)
                    if (!inexactActive || storedInexact != nextSlot) {
                        AlarmSchedulerV3.scheduleInexactAlarm(
                            application,
                            INEXACT_ALARM_ID,
                            nextHour,
                            nextMinute
                        )
                        Log.i(
                            TAG,
                            "onInexactAlarmReceived: exact active — inexact rescheduled to $nextHour:$nextMinute"
                        )
                    } else {
                        Log.i(
                            TAG,
                            "onInexactAlarmReceived: exact active — inexact already at $nextHour:$nextMinute, no change"
                        )
                    }
                }

                exactPermissionGranted && !exactAlarmActive -> {
                    // Sync needed — permission is granted but the exact alarm was somehow lost.
                    // Restore both alarms silently (next exact fire will show the notification).
                    val result = scheduleNextAlarm()
                    Log.i(TAG, "onInexactAlarmReceived: exact alarm missing — $result")
                }

                else -> {
                    // Fallback — exact permission is revoked; inexact acts as the active alarm.
                    // scheduleNextAlarm() only schedules the inexact here (exact skipped, no permission).
                    val result = scheduleNextAlarm()
                    Log.i(
                        TAG,
                        "onInexactAlarmReceived: no exact permission — fallback notification — $result"
                    )
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
     * Compares the next computed slot against the currently stored slot for each alarm.
     * Only schedules an alarm if it is missing or pointing at a different slot.
     *
     * @return [ScheduleResult.Unchanged] when both alarms were already correct,
     *         [ScheduleResult.Rescheduled] with details of what was actually updated.
     */
    private fun scheduleNextAlarm(): ScheduleResult {
        val (hour, minute) = getNextSlot()
        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val nextSlot = Pair(hour, minute)

        val exactActive = AlarmSchedulerV3.isExactAlarmActive(application, EXACT_ALARM_ID)
        val inexactActive = AlarmSchedulerV3.isInexactAlarmActive(application, INEXACT_ALARM_ID)
        val storedExact = AlarmSchedulerV3.getStoredExactSlot(application, EXACT_ALARM_ID)
        val storedInexact = AlarmSchedulerV3.getStoredInexactSlot(application, INEXACT_ALARM_ID)

        // Exact alarm needs updating if we have permission and it is missing or stale.
        val exactNeedsUpdate = permissionGranted && (!exactActive || storedExact != nextSlot)
        // Inexact alarm always needs to be alive; update if missing or stale.
        val inexactNeedsUpdate = !inexactActive || storedInexact != nextSlot

        if (!exactNeedsUpdate && !inexactNeedsUpdate) {
            return ScheduleResult.Unchanged(hour, minute)
        }

        if (exactNeedsUpdate) {
            if (!exactActive) {
                Log.i(
                    TAG,
                    "scheduleNextAlarm: exact alarm was missing — restoring at %02d:%02d (permission re-granted or alarm lost)".format(
                        hour,
                        minute
                    )
                )
            } else {
                val oldTime =
                    storedExact?.let { "%02d:%02d".format(it.first, it.second) } ?: "unknown"
                Log.i(
                    TAG,
                    "scheduleNextAlarm: exact alarm at wrong time — fixing $oldTime → %02d:%02d".format(
                        hour,
                        minute
                    )
                )
            }
            AlarmSchedulerV3.scheduleExactAlarm(application, EXACT_ALARM_ID, hour, minute)
        }
        if (inexactNeedsUpdate) {
            if (inexactActive) {
                val oldTime =
                    storedInexact?.let { "%02d:%02d".format(it.first, it.second) } ?: "unknown"
                Log.i(
                    TAG,
                    "scheduleNextAlarm: inexact alarm at wrong time — fixing $oldTime → %02d:%02d".format(
                        hour,
                        minute
                    )
                )
            }
            AlarmSchedulerV3.scheduleInexactAlarm(application, INEXACT_ALARM_ID, hour, minute)
        }

        return ScheduleResult.Rescheduled(
            hour,
            minute,
            exact = exactNeedsUpdate,
            inexact = inexactNeedsUpdate
        )
    }

    private sealed interface ScheduleResult {
        data class Rescheduled(
            val hour: Int,
            val minute: Int,
            val exact: Boolean,
            val inexact: Boolean,
        ) : ScheduleResult {
            override fun toString() = "Rescheduled → %02d:%02d [exact=%s inexact=%s]".format(
                hour, minute,
                if (exact) "updated" else "skipped",
                if (inexact) "updated" else "skipped",
            )
        }

        data class Unchanged(val hour: Int, val minute: Int) : ScheduleResult {
            override fun toString() = "Unchanged → already at %02d:%02d".format(hour, minute)
        }
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
