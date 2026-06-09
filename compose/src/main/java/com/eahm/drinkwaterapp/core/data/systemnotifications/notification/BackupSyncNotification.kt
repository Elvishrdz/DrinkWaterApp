package com.eahm.drinkwaterapp.core.data.systemnotifications.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.MainActivity
import com.eahm.drinkwaterapp.core.data.systemnotifications.channel.BackupSyncNotificationChannel
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.BackupSyncNotification.show
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications

/**
 * Dedicated, single-id notification mirroring the background backup status. Calling [show]
 * again with new text updates the same notification in place rather than stacking new ones.
 */
internal object BackupSyncNotification {
    const val NOTIFICATION_ID = 4242

    @SuppressLint("MissingPermission")
    fun show(
        context: Context,
        title: String,
        text: String,
        ongoing: Boolean,
    ) {
        if (!context.canShowNotifications) return

        val builder = NotificationCompat.Builder(context, BackupSyncNotificationChannel.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .setContentIntent(contentIntent(context))

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
