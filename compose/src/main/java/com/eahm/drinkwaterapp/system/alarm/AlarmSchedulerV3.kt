package com.eahm.drinkwaterapp.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.content.edit
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import java.util.Calendar

/**
 * Scheduler for the dual-alarm system (V2 manager).
 *
 * Manages two independent alarms:
 *  - Exact  alarm: [AlarmReceiver.EXACT_ALARM_ACTION]  — uses setExactAndAllowWhileIdle
 *  - Inexact alarm: [AlarmReceiver.INEXACT_ALARM_ACTION] — uses setAndAllowWhileIdle (guard/fallback)
 *
 * Each alarm has its own PendingIntent (distinct action + requestCode), so they are fully
 * independent from each other and from the legacy [AlarmScheduler] / [AlarmSchedulerV2].
 *
 * This scheduler contains no business logic. It receives a pre-calculated target time
 * (hour, minute) and registers it with AlarmManager.
 */
object AlarmSchedulerV3 {

    private const val PREFS_NAME = "alarms_v3"

    // region Schedule

    fun scheduleExactAlarm(context: Context, alarmId: Int, hour: Int, minute: Int) {
        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            val delayMs = delayUntilNext(hour, minute)
            val triggerAtMs = SystemClock.elapsedRealtime() + delayMs
            val pendingIntent = exactPendingIntent(context, alarmId, hour, minute, update = true)
                ?: return

            context.getSystemService(AlarmManager::class.java)
                .setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMs,
                    pendingIntent,
                )

            saveToStorage(context, alarmId, prefix = "exact", hour, minute)

        } else {
            throw Exception("This should never happen")
        }
    }

    fun scheduleInexactAlarm(context: Context, alarmId: Int, hour: Int, minute: Int) {
        val delayMs = delayUntilNext(hour, minute)
        val triggerAtMs = SystemClock.elapsedRealtime() + delayMs
        val pendingIntent = inexactPendingIntent(context, alarmId, hour, minute, update = true)
            ?: return

        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMs,
                pendingIntent,
            )

        saveToStorage(context, alarmId, prefix = "inexact", hour, minute)
    }

    // endregion

    // region Cancel

    fun cancelExactAlarm(context: Context, alarmId: Int) {
        exactPendingIntent(context, alarmId, 0, 0, update = false)?.let {
            context.getSystemService(AlarmManager::class.java).cancel(it)
            it.cancel()
        }
        clearFromStorage(context, alarmId, prefix = "exact")
    }

    fun cancelInexactAlarm(context: Context, alarmId: Int) {
        inexactPendingIntent(context, alarmId, 0, 0, update = false)?.let {
            context.getSystemService(AlarmManager::class.java).cancel(it)
            it.cancel()
        }
        clearFromStorage(context, alarmId, prefix = "inexact")
    }

    fun cancelAllAlarms(context: Context, exactAlarmId: Int, inexactAlarmId: Int) {
        cancelExactAlarm(context, exactAlarmId)
        cancelInexactAlarm(context, inexactAlarmId)
    }

    // endregion

    // region Status

    fun isExactAlarmActive(context: Context, alarmId: Int): Boolean =
        exactPendingIntent(context, alarmId, 0, 0, update = false) != null

    fun isInexactAlarmActive(context: Context, alarmId: Int): Boolean =
        inexactPendingIntent(context, alarmId, 0, 0, update = false) != null

    fun areAlarmsActive(context: Context, exactAlarmId: Int, inexactAlarmId: Int): Boolean =
        isExactAlarmActive(context, exactAlarmId) && isInexactAlarmActive(context, inexactAlarmId)

    /** Returns the last slot saved when this exact alarm was scheduled, or null if never stored. */
    fun getStoredExactSlot(context: Context, alarmId: Int): Pair<Int, Int>? =
        readFromStorage(context, alarmId, prefix = "exact")

    /** Returns the last slot saved when this inexact alarm was scheduled, or null if never stored. */
    fun getStoredInexactSlot(context: Context, alarmId: Int): Pair<Int, Int>? =
        readFromStorage(context, alarmId, prefix = "inexact")

    // endregion

    // region Private helpers

    /** Returns milliseconds until the next occurrence of [hour]:[minute] local time. */
    private fun delayUntilNext(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }

    private fun exactPendingIntent(
        context: Context,
        alarmId: Int,
        hour: Int,
        minute: Int,
        update: Boolean,
    ): PendingIntent? = PendingIntent.getBroadcast(
        context,
        alarmId,
        Intent(AlarmReceiver.EXACT_ALARM_ACTION).apply {
            setPackage(context.packageName)
            putExtra(AlarmReceiver.ALARM_EXTRAS_TRIGGER_HOUR, hour)
            putExtra(AlarmReceiver.ALARM_EXTRAS_TRIGGER_MINUTE, minute)
        },
        if (update) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun inexactPendingIntent(
        context: Context,
        alarmId: Int,
        hour: Int,
        minute: Int,
        update: Boolean,
    ): PendingIntent? = PendingIntent.getBroadcast(
        context,
        alarmId,
        Intent(AlarmReceiver.INEXACT_ALARM_ACTION).apply {
            setPackage(context.packageName)
            putExtra(AlarmReceiver.ALARM_EXTRAS_TRIGGER_HOUR, hour)
            putExtra(AlarmReceiver.ALARM_EXTRAS_TRIGGER_MINUTE, minute)
        },
        if (update) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun saveToStorage(
        context: Context,
        alarmId: Int,
        prefix: String,
        hour: Int,
        minute: Int,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putInt("${prefix}_hour_$alarmId", hour)
            putInt("${prefix}_minute_$alarmId", minute)
        }
    }

    private fun clearFromStorage(context: Context, alarmId: Int, prefix: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove("${prefix}_hour_$alarmId")
            remove("${prefix}_minute_$alarmId")
        }
    }

    private fun readFromStorage(context: Context, alarmId: Int, prefix: String): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt("${prefix}_hour_$alarmId", -1)
        val minute = prefs.getInt("${prefix}_minute_$alarmId", -1)
        return if (hour != -1 && minute != -1) Pair(hour, minute) else null
    }

    // endregion
}
