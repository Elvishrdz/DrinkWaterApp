package com.eahm.drinkwaterapp.system.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Utility for checking and requesting the SCHEDULE_EXACT_ALARM permission.
 *
 * On API < 31 exact alarms are always available without any special permission.
 * On API 31+ the user must explicitly grant permission via system settings.
 */
object ExactAlarmPermission {

    /**
     * Returns true if exact alarms can be scheduled right now.
     * Always true on API < 31.
     */
    fun isGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

    /**
     * Launches the system settings screen where the user can grant
     * SCHEDULE_EXACT_ALARM for this app. No-op on API < 31.
     */
    fun openSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
