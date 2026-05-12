package com.eahm.drinkwaterapp.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.edit
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.createWithSeconds
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.scheduleAlarm
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver.Companion.ALARM_EXTRAS_HOUR
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver.Companion.ALARM_EXTRAS_MINUTE
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

object AlarmScheduler {

    /**
     * Schedules an alarm to fire at the next occurrence of [hour]:[minute] in local time.
     * If that time has already passed today, the alarm is scheduled for the same time tomorrow.
     * Delegates to [scheduleAlarm] with a computed delay so storage and registration are shared.
     */
    fun scheduleAlarm(context: Context, alarmId: Int, hour: Int, minute: Int, second: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis
        scheduleAlarm(context, alarmId, delayMs)
    }

    /**
     * @param  [millisToTriggerFromNow] get the time to trigger this alarm in milliseconds. The time
     * is calculated from the moment the alarm is scheduled. example. passing 5000 will trigger this alarm
     * in 5 seconds from the moment is called.
     * */
    fun scheduleAlarm(context: Context, alarmId: Int, millisToTriggerFromNow: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val triggerAt = SystemClock.elapsedRealtime() + millisToTriggerFromNow

        val triggerTime = TimeResult.createWithSeconds(
            seconds = millisToTriggerFromNow.milliseconds.inWholeSeconds,
        )

        val intent = getAlarmIntent(
            context = context,
            alarmId = alarmId,
            millisToTriggerFromNow = millisToTriggerFromNow,
            hour = triggerTime.hour,
            minute = triggerTime.minute,
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pendingIntent
        )

        saveAlarmToStorage(context, alarmId, millisToTriggerFromNow, triggerAt)
    }

    fun getAlarmIntent(
        context: Context,
        alarmId: Int,
        millisToTriggerFromNow: Long,
        hour: Int,
        minute: Int,
    ): Intent {
        return Intent(AlarmReceiver.ALARM_ACTION).apply {
            setPackage(context.packageName)
            putExtra(ALARM_EXTRAS_HOUR, hour)
            putExtra(ALARM_EXTRAS_MINUTE, minute)
            putExtra("alarm_id", alarmId)
            putExtra("interval_ms", millisToTriggerFromNow)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(AlarmReceiver.ALARM_ACTION).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        removeAlarmFromStorage(context, alarmId)
    }

    // TODO: verify this is working
    fun isAlarmActive(context: Context, alarmId: Int): Boolean {
        val intent = Intent(AlarmReceiver.ALARM_ACTION).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmExists = pendingIntent != null

        return alarmExists
    }

    fun getTimeUntilNextTrigger(context: Context, alarmId: Int): Long? {
        if (!isAlarmActive(context, alarmId)) return null

        val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val triggerAtElapsed = prefs.getLong("alarm_trigger_at_$alarmId", -1L)
        if (triggerAtElapsed == -1L) return null

        val remaining = triggerAtElapsed - SystemClock.elapsedRealtime()
        return if (remaining > 0) remaining else 0L
    }

    private fun saveAlarmToStorage(
        context: Context,
        alarmId: Int,
        triggerTimeMillis: Long,
        triggerAt: Long,
    ) {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val ids = prefs.getStringSet(KEY_ACTIVE_ALARMS, mutableSetOf())!!
            .toMutableSet().also { it.add(alarmId.toString()) }

        prefs.edit {
            putStringSet(KEY_ACTIVE_ALARMS, ids)
                .putLong(keyTriggerTimeMillis(alarmId), triggerTimeMillis)
                .putLong(keyTriggerAtMillis(alarmId), triggerAt)
        }
    }

    fun getAlarmStorageInfos(context: Context): List<AlarmStorageInfo> {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val activeAlarms = prefs.getStringSet(KEY_ACTIVE_ALARMS, emptySet()).orEmpty()

        return activeAlarms
            .mapNotNull { runCatching { it.toInt() }.getOrNull() }
            .mapNotNull { alarmId ->
                getAlarmStorageInfo(context, alarmId)
            }
    }

    @OptIn(ExperimentalTime::class)
    fun getAlarmStorageInfo(context: Context, alarmId: Int): AlarmStorageInfo? {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val activeAlarms = prefs.getStringSet(KEY_ACTIVE_ALARMS, emptySet())

        val id = activeAlarms
            ?.find { it.equals(alarmId.toString(), true) }
            ?.toInt()

        if (id == null) {
            return null // not founded
        }

        val triggerTimeMillis = prefs.getLong(keyTriggerTimeMillis(id), -1)

        val triggerTime = TimeResult.createWithSeconds(
            seconds = triggerTimeMillis.milliseconds.inWholeSeconds,
        )

        val triggerAtMillis = prefs.getLong(keyTriggerAtMillis(id), -1)
        val wallClockMs = prefs.getLong(keyWallClockMs(id), -1L)

        // Prefer wall-clock ms: elapsedRealtime resets on reboot so triggerAtMillis is stale after a reboot.
        val alarmTriggerAt = if (wallClockMs > 0L) {
            val calendar = Calendar.getInstance().apply { timeInMillis = wallClockMs }
            AlarmTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                second = calendar.get(Calendar.SECOND),
            )
        } else {
            triggerAtMillis.asAlarmTime
        }.let {
            TimeResult(
                hour = it.hour,
                minute = it.minute,
                second = it.second,
                millisecond = it.second.toLong(),
            )
        }

        return AlarmStorageInfo(
            alarmId = id,
            alarmTriggerTimeMillis = triggerTimeMillis,
            alarmTriggerTime = triggerTime,
            alarmTriggerAtMillis = triggerAtMillis,
            alarmTriggerAt = alarmTriggerAt,
        )
    }

    // triggerAtMillis is an elapsedRealtime value, not a Unix timestamp.
    // Offset it to wall-clock time, then extract hour and minute.
    val Long.asDayTime
        get() = if (this <= 0) {
            DayTime(0, 0)
        } else {
            val wallClockMs = System.currentTimeMillis() +
                    (this - SystemClock.elapsedRealtime())

            val calendar = Calendar.getInstance().apply {
                timeInMillis = wallClockMs
            }

            DayTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
            )
        }

    // triggerAtMillis is an elapsedRealtime value, not a Unix timestamp.
    // Offset it to wall-clock time, then extract hour and minute.
    val Long.asAlarmTime
        get() = if (this <= 0) {
            AlarmTime(0, 0, 0)
        } else {
            val wallClockMs = System.currentTimeMillis() +
                    (this - SystemClock.elapsedRealtime())

            val calendar = Calendar.getInstance().apply {
                timeInMillis = wallClockMs
            }

            AlarmTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                second = calendar.get(Calendar.SECOND),
            )
        }

    private fun removeAlarmFromStorage(context: Context, alarmId: Int) {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val ids = prefs.getStringSet(KEY_ACTIVE_ALARMS, mutableSetOf())!!
            .toMutableSet().also { it.remove(alarmId.toString()) }

        prefs.edit {
            putStringSet(KEY_ACTIVE_ALARMS, ids)
                .remove(keyTriggerTimeMillis(alarmId))
                .remove(keyTriggerAtMillis(alarmId))
                .remove(keyWallClockMs(alarmId))
        }
    }

    const val ALARM_PREFS_NAME = "alarms"
    const val KEY_ACTIVE_ALARMS = "active_alarms"

    fun keyTriggerTimeMillis(alarmId: Int) = "alarm_trigger_time_ms_$alarmId"
    fun keyTriggerAtMillis(alarmId: Int) = "alarm_trigger_at_$alarmId"
    fun keyWallClockMs(alarmId: Int) = "alarm_wall_clock_ms_$alarmId"
}

data class AlarmStorageInfo(
    val alarmId: Int,
    val alarmTriggerTimeMillis: Long,
    val alarmTriggerTime: TimeResult,
    val alarmTriggerAtMillis: Long,
    val alarmTriggerAt: TimeResult,
)
