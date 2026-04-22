package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.content.Context
import androidx.core.content.edit

object AlarmIntervals {

    fun getInterval(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val currentHour = prefs.getInt(ALARM_INTERVAL_HOUR, ALARM_INTERVAL_HOUR_DEFAULT)
        val currentMinute = prefs.getInt(ALARM_INTERVAL_MINUTE, ALARM_INTERVAL_MINUTE_DEFAULT)

        return Pair(currentHour, currentMinute)
    }

    fun setInterval(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit {
            putInt(ALARM_INTERVAL_HOUR, hour)
                .putInt(ALARM_INTERVAL_MINUTE, minute)
        }
    }

    fun clearInterval(context: Context) {
        setNextInterval(context, ALARM_INTERVAL_HOUR_DEFAULT, ALARM_INTERVAL_MINUTE_DEFAULT)
    }

    fun getNextInterval(context: Context): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        val currentHour =
            prefs.getInt(ALARM_INTERVAL_NEXT_HOUR, ALARM_INTERVAL_NEXT_HOUR_DEFAULT)
        val currentMinute =
            prefs.getInt(ALARM_INTERVAL_NEXT_MINUTE, ALARM_INTERVAL_NEXT_MINUTE_DEFAULT)

        return currentHour
            .takeIf { it != -1 }
            ?.let { hour ->
                currentMinute
                    .takeIf { it != -1 }
                    ?.let { minute ->
                        Pair(hour, minute)
                    }
            }
    }

    fun clearNextInterval(context: Context) {
        setNextInterval(
            context,
            ALARM_INTERVAL_NEXT_HOUR_DEFAULT,
            ALARM_INTERVAL_NEXT_MINUTE_DEFAULT
        )
    }

    fun setNextInterval(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit {
            putInt(ALARM_INTERVAL_NEXT_HOUR, hour)
                .putInt(ALARM_INTERVAL_NEXT_MINUTE, minute)
        }
    }

    const val ALARM_PREFS_NAME = "alarms"
    const val ALARM_INTERVAL_HOUR_DEFAULT = 0
    const val ALARM_INTERVAL_MINUTE_DEFAULT = 5
    const val ALARM_INTERVAL_HOUR = "interval_hour"
    const val ALARM_INTERVAL_MINUTE = "interval_minute"
    const val ALARM_INTERVAL_NEXT_HOUR_DEFAULT = -1
    const val ALARM_INTERVAL_NEXT_MINUTE_DEFAULT = -1
    const val ALARM_INTERVAL_NEXT_HOUR = "next_interval_hour"
    const val ALARM_INTERVAL_NEXT_MINUTE = "next_interval_minute"
}
