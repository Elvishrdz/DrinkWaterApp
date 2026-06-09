package com.eahm.drinkwaterapp.core.data.systemnotifications.channel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Low-importance channel for the (dev-only, for now) background-backup status notification.
 * Silent and non-intrusive — it only mirrors what the background worker is doing.
 */
internal object BackupSyncNotificationChannel {
    const val CHANNEL_ID = "backup_sync_status"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Backup status",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows the status of background data backups."
            enableVibration(false)
            setShowBadge(false)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
