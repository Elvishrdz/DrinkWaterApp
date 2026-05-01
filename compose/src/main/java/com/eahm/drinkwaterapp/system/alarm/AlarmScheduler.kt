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
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlin.time.Duration.Companion.milliseconds

object AlarmScheduler {

    fun scheduleAlarm(context: Context, alarmId: Int, intervalMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        val triggerAt = SystemClock.elapsedRealtime() + intervalMs

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            Intent(AlarmReceiver.ALARM_ACTION).apply {
                setPackage(context.packageName)
                putExtra("alarm_id", alarmId)
                putExtra("interval_ms", intervalMs)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            pendingIntent
        )

        saveAlarmToStorage(context, alarmId, intervalMs, triggerAt)
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

    private fun removeAlarmFromStorage(context: Context, alarmId: Int) {
        val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val ids = prefs.getStringSet("active_alarms", mutableSetOf())!!
            .toMutableSet().also { it.remove(alarmId.toString()) }
        prefs.edit {
            putStringSet("active_alarms", ids)
                .remove("alarm_interval_$alarmId")
        }
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
        intervalMs: Long,
        triggerAt: Long,
    ) {
        val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val ids = prefs.getStringSet("active_alarms", mutableSetOf())!!
            .toMutableSet().also { it.add(alarmId.toString()) }

        val seconds = intervalMs.milliseconds.inWholeSeconds.toInt()
        val dayTime = DayTime.createWithSeconds(seconds)

        prefs.edit {
            putStringSet("active_alarms", ids)
                .putLong("alarm_interval_$alarmId", intervalMs)
                .putLong("alarm_trigger_at_$alarmId", triggerAt)
                .putInt("alarm_trigger_at_hour_$alarmId", dayTime.hour)
                .putInt("alarm_trigger_at_minute_$alarmId", dayTime.minute)
        }
    }
}
