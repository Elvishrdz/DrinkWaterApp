package com.eahm.drinkwaterapp.core.data.systemnotifications.channel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.eahm.drinkwaterapp.R

internal object DrinkNotificationChannel {
    const val DRINK_CHANNEL_ID = "100"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channelId = DRINK_CHANNEL_ID
        val name = context.getString(R.string.app_name)
        val description = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        context
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
