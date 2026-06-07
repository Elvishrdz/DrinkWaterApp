package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.util.Log
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2.Companion.EXACT_ALARM_ID
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2.Companion.INEXACT_ALARM_ID
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.getNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
 * Slot calculation is driven by [PrefsStorage] (interval, start/end time) and [AppDatabase]
 * (daily goal), delegating to [getNextAlarmSlot] from WhatToDoNowDecider.
 */
@OptIn(ExperimentalTime::class)
class DrinkAlarmManagerV2(
    private val application: Application,
    private val prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) {

    private val schedulingMutex = Mutex()

    // region Public API

    /**
     * Called on boot, package replaced, permission re-granted, or settings changed.
     * Schedules both alarms if either is missing or pointing at a stale slot.
     *
     * No-op until onboarding is completed so that system broadcasts received during the
     * first-launch setup flow do not schedule alarms with unconfigured default values.
     */
    suspend fun restoreAlarm(source: String = "unknown") {
        if (!prefsStorage.getOnboardingCompleted().first()) {
            Log.i(TAG, "restoreAlarm: onboarding not completed — skipping (source=$source)")
            return
        }
        Log.i(TAG, "restoreAlarm: source=$source — waiting for lock")
        schedulingMutex.withLock {
            Log.i(TAG, "restoreAlarm: source=$source — lock acquired")
            val result = scheduleNextAlarm()
            Log.i(TAG, "restoreAlarm: source=$source — $result")
        }
    }

    /**
     * Called when the exact alarm (EXACT_ALARM_ACTION) fires.
     * Reschedules both alarms and shows the notification — only if exact permission is still granted
     * AND today is an active day. If the user disabled today after the alarm was already scheduled,
     * we skip the notification and immediately jump to the next active day.
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

            // Suppress notification if today became inactive after this alarm was scheduled.
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val todayIsoDay = now.dayOfWeek.ordinal + 1 // Mon=1 … Sun=7
            val activeDays = prefsStorage.getActiveDays().first()
            if (todayIsoDay in activeDays) {
                NotificationService.showDrinkNotification(application, hour, minute)
            } else {
                Log.i(
                    TAG,
                    "onAlarmReceived: today (ISO $todayIsoDay) is inactive — notification suppressed"
                )
            }
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
    @Suppress("CyclomaticComplexMethod", "LongMethod", "CognitiveComplexMethod")
    suspend fun onInexactAlarmReceived(source: AlarmSource, hour: Int?, minute: Int?) {
        schedulingMutex.withLock {
            // Always clear the previous receipt first, then record how early or late
            // this alarm fired relative to its scheduled time.
            AlarmSchedulerV3.clearInexactReceipt(application, INEXACT_ALARM_ID)
            if (hour != null && minute != null) {
                val now = Calendar.getInstance()
                val scheduled = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    // No day rollover — we want today's scheduled time to compute the real diff.
                }
                val deltaMs = scheduled.timeInMillis - now.timeInMillis
                AlarmSchedulerV3.saveInexactReceipt(
                    application,
                    INEXACT_ALARM_ID,
                    deltaMs,
                    hour,
                    minute
                )
                val logMsg = when {
                    deltaMs > 1_000 -> "fired ${deltaMs}ms early"
                    deltaMs < -1_000 -> "fired ${-deltaMs}ms late"
                    else -> "fired on time"
                }
                Log.i(
                    TAG,
                    "onInexactAlarmReceived: $logMsg (scheduled at %02d:%02d)".format(hour, minute)
                )
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
                    val nextSlotData = getNextSlot()
                    val nextHour = nextSlotData.hour
                    val nextMinute = nextSlotData.minute
                    val nextTargetCalendar = nextSlotData.targetCalendar
                    val nextSlot = Pair(nextHour, nextMinute)
                    val inexactActive =
                        AlarmSchedulerV3.isInexactAlarmActive(application, INEXACT_ALARM_ID)
                    val storedInexact =
                        AlarmSchedulerV3.getStoredInexactSlot(application, INEXACT_ALARM_ID)
                    // When skipping inactive days, always reschedule — H:M alone can't distinguish
                    // different future dates that share the same start-time H:M.
                    val needsUpdate =
                        !inexactActive || storedInexact != nextSlot || nextTargetCalendar != null
                    if (needsUpdate) {
                        if (nextTargetCalendar != null) {
                            AlarmSchedulerV3.scheduleInexactAlarmAt(
                                application,
                                INEXACT_ALARM_ID,
                                nextTargetCalendar,
                            )
                        } else {
                            AlarmSchedulerV3.scheduleInexactAlarm(
                                application,
                                INEXACT_ALARM_ID,
                                nextHour,
                                nextMinute,
                            )
                        }
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
    @Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod", "MagicNumber")
    private suspend fun scheduleNextAlarm(): ScheduleResult {
        val nextSlotData = getNextSlot()
        val hour = nextSlotData.hour
        val minute = nextSlotData.minute
        val targetCalendar = nextSlotData.targetCalendar
        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val nextSlot = Pair(hour, minute)

        val exactActive = AlarmSchedulerV3.isExactAlarmActive(application, EXACT_ALARM_ID)
        val inexactActive = AlarmSchedulerV3.isInexactAlarmActive(application, INEXACT_ALARM_ID)
        val storedExact = AlarmSchedulerV3.getStoredExactSlot(application, EXACT_ALARM_ID)
        val storedInexact = AlarmSchedulerV3.getStoredInexactSlot(application, INEXACT_ALARM_ID)

        // When targetCalendar is set we are skipping inactive days. H:M alone cannot distinguish
        // two different future dates that happen to share the same start-time H:M (e.g. the alarm
        // was already pointing at next Monday's startTime, but now we need to jump to Tuesday's).
        // Force a reschedule in that case so the Calendar target is always applied.
        val alwaysReschedule = targetCalendar != null
        // Exact alarm needs updating if we have permission and it is missing or stale.
        val exactNeedsUpdate =
            permissionGranted && (alwaysReschedule || !exactActive || storedExact != nextSlot)
        // Inexact alarm always needs to be alive; update if missing or stale.
        val inexactNeedsUpdate = alwaysReschedule || !inexactActive || storedInexact != nextSlot

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
            if (targetCalendar != null) {
                AlarmSchedulerV3.scheduleExactAlarmAt(application, EXACT_ALARM_ID, targetCalendar)
            } else {
                AlarmSchedulerV3.scheduleExactAlarm(application, EXACT_ALARM_ID, hour, minute)
            }
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
            if (targetCalendar != null) {
                AlarmSchedulerV3.scheduleInexactAlarmAt(
                    application,
                    INEXACT_ALARM_ID,
                    targetCalendar
                )
            } else {
                AlarmSchedulerV3.scheduleInexactAlarm(application, INEXACT_ALARM_ID, hour, minute)
            }
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
     * Reads the user's preferences (start time, end time, interval, active days) and today's goal
     * from the database, then delegates to [getNextAlarmSlot] to compute the next alarm time.
     *
     * If the calculated slot falls on an inactive day, returns a [Calendar] pointing to the start
     * of the nearest future active day so the caller can schedule with exact millisecond precision.
     *
     * Returns (hour, minute, targetCalendar?) — [targetCalendar] is non-null only when the next
     * slot is on an inactive day and the caller must use [AlarmSchedulerV3.scheduleExactAlarmAt].
     */
    @Suppress("MagicNumber")
    private suspend fun getNextSlot(): NextSlot {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val intervalPrefs = prefsStorage.getInterval().first()
        val interval = AlarmInterval(intervalPrefs.hour, intervalPrefs.minute)
        val startTime = prefsStorage.getStartTime().first().toLocalDateTime()
        val endTime = prefsStorage.getEndTime().first().toLocalDateTime()
        val todayEpoch = now.date.toEpochDays()
        val goalMl = database.getDailyGoalDao().getGoalForDate(todayEpoch).first()
            ?.goalMl?.takeIf { it > 0f } ?: DEFAULT_GOAL_ML
        val activeDays = prefsStorage.getActiveDays().first()

        val nextSlotTime = getNextAlarmSlot(now, startTime, endTime, goalMl, interval)
        // kotlinx.datetime DayOfWeek.ordinal is 0-based (Mon=0), convert to ISO 1-based (Mon=1)
        val slotIsoDay = nextSlotTime.dayOfWeek.ordinal + 1

        if (slotIsoDay in activeDays) {
            return NextSlot(nextSlotTime.hour, nextSlotTime.minute, targetCalendar = null)
        }

        // The next slot falls on an inactive day — find the nearest upcoming active day.
        var daysAhead = 1
        for (offset in 1..6) {
            val candidate = ((slotIsoDay - 1 + offset) % 7) + 1 // ISO 1-based wrap
            if (candidate in activeDays) {
                daysAhead = offset
                break
            }
        }
        Log.i(
            TAG,
            "getNextSlot: ISO day $slotIsoDay inactive — skipping $daysAhead day(s) to next active day"
        )

        val target = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysAhead)
            set(Calendar.HOUR_OF_DAY, startTime.hour)
            set(Calendar.MINUTE, startTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return NextSlot(startTime.hour, startTime.minute, targetCalendar = target)
    }

    private data class NextSlot(val hour: Int, val minute: Int, val targetCalendar: Calendar?)

    // endregion

    companion object {
        /** Request code for the exact alarm PendingIntent. */
        const val EXACT_ALARM_ID = 127

        /** Request code for the inexact guard alarm PendingIntent. */
        const val INEXACT_ALARM_ID = 128

        private const val TAG = "DrinkAlarmManagerV2"
    }
}
