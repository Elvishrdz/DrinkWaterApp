package com.eahm.drinkwaterapp.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.edit
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.createWithSeconds
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.scheduleAlarm
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

object AlarmScheduler {


    /**
     * Schedules an alarm to fire at the next occurrence of [hour]:[minute] in local time.
     * If that time has already passed today, the alarm is scheduled for the same time tomorrow.
     * Delegates to [scheduleAlarm] with a computed delay so storage and registration are shared.
     */
    fun scheduleAlarm(context: Context, alarmId: Int, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis
        scheduleAlarm(context, alarmId, delayMs)
    }

    /**
     * @param  [triggerInTimeFromNowMillis] get the time to trigger this alarm in milliseconds. The time
     * is calculated from the moment the alarm is scheduled. example. passing 5000 will trigger this alarm
     * in 5 seconds from the moment is called.
     * */
    fun scheduleAlarm(context: Context, alarmId: Int, triggerInTimeFromNowMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        val triggerAt = SystemClock.elapsedRealtime() + triggerInTimeFromNowMillis

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(AlarmReceiver.ALARM_ACTION).apply {
                setPackage(context.packageName)
                putExtra("alarm_id", alarmId)
                putExtra("interval_ms", triggerInTimeFromNowMillis)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pendingIntent
        )

        saveAlarmToStorage(context, alarmId, triggerInTimeFromNowMillis, triggerAt)
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(AlarmReceiver.ALARM_ACTION).setPackage(context.packageName),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        removeAlarmFromStorage(context, alarmId)
    }

    fun isAlarmActive(context: Context, alarmId: Int): Boolean {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(AlarmReceiver.ALARM_ACTION).setPackage(context.packageName),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
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

        val triggerTime = DayTime.createWithSeconds(
            seconds = triggerTimeMillis.milliseconds.inWholeSeconds.toInt(),
        )

        val triggerAtMillis = prefs.getLong(keyTriggerAtMillis(id), -1)

        // triggerAtMillis is an elapsedRealtime value, not a Unix timestamp.
        // Offset it to wall-clock time, then extract hour and minute.
        val triggerAt = if (triggerAtMillis == -1L) {
            DayTime(0, 0)
        } else {
            val wallClockMs = System.currentTimeMillis() +
                    (triggerAtMillis - SystemClock.elapsedRealtime())

            val calendar = Calendar.getInstance().apply {
                timeInMillis = wallClockMs
            }

            DayTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
            )
        }

        return AlarmStorageInfo(
            alarmId = id,
            alarmTriggerTimeMillis = triggerTimeMillis,
            alarmTriggerTime = triggerTime,
            alarmTriggerAtMillis = triggerAtMillis,
            alarmTriggerAt = triggerAt,
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
        }
    }

    const val ALARM_PREFS_NAME = "alarms"
    const val KEY_ACTIVE_ALARMS = "active_alarms"

    fun keyTriggerTimeMillis(alarmId: Int) = "alarm_trigger_time_ms_$alarmId"
    fun keyTriggerAtMillis(alarmId: Int) = "alarm_trigger_at_$alarmId"
}

data class AlarmStorageInfo(
    val alarmId: Int,
    val alarmTriggerTimeMillis: Long,
    val alarmTriggerTime: DayTime,
    val alarmTriggerAtMillis: Long,
    val alarmTriggerAt: DayTime,
)
