package com.eahm.drinkwaterapp.core.data.systemnotifications.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.MainActivity
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.systemnotifications.channel.DrinkNotificationChannel
import com.eahm.drinkwaterapp.core.data.systemnotifications.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationClicked
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationConfig
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlin.random.Random

internal object DrinkNotification {
    const val NOTIFICATION_ID = 1
    const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"

    fun showNotification(
        context: Context,
        title: String?,
        text: String?,
        bigText: String?
    ) {
        if (!context.canShowNotifications) return

        val channelId = DrinkNotificationChannel.DRINK_CHANNEL_ID
        val notificationId = NOTIFICATION_ID

        val config = getNotificationConfig(context)

        val contentIntent = getOnNotificationClickedPendingIntent(context)
        val skipIntent = getOnSkipPendingIntent(context)
        val drinkIntent = getOnDrinkPendingIntent(context)
        val deleteIntent = getOnNotificationDeletedPendingIntent(context)

        val builder = NotificationCompat.Builder(context, channelId)
            //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
            .setSmallIcon(config.smallIcon)
            .setLargeIcon(config.largeIcon)
            .setColor(config.color)
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentIntent)
            .addAction(
                0,
                context.resources.getString(R.string.text_skip),
                skipIntent,
            ) //R.drawable.icon_skip
            .addAction(
                0,
                context.resources.getString(R.string.text_drink),
                drinkIntent
            ) //R.drawable.icon_drink
            //.setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //.setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setDeleteIntent(deleteIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        }

        val uriAudio =
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.app_notification_sound)
        val arrayVibrate = longArrayOf(1000, 1000)

        builder.setSound(uriAudio)
        builder.setVibrate(arrayVibrate)
        builder.setLights(Color.RED, 3000, 3000)

        NotificationManagerCompat.from(context)
            .notify(notificationId, builder.build())
    }

    private fun getNotificationConfig(context: Context): NotificationConfig {
        val goalReached = false  // TODO: if (SettingsClass().isGoalReached())
        val code = Random(7).nextInt(1, 5)

        val colorResId = getColorResId(goalReached, code)
        val color = context.resources.getColor(colorResId)

        val smallIconResId = getSmallIconResId(goalReached, code)

        val largeIconResId = if (goalReached) {
            R.mipmap.ic_app_2
        } else {
            R.mipmap.ic_launcher
        }

        val largeIcon = BitmapFactory.decodeResource(context.resources, largeIconResId)

        return NotificationConfig(
            color = color,
            smallIcon = smallIconResId,
            largeIcon = largeIcon,
        )
    }

    private fun getColorResId(completed: Boolean, code: Int): Int {
        return if (completed) {
            when (code) {
                1 -> R.color.purple
                2 -> R.color.blue
                3 -> R.color.black
                4 -> R.color.grey_3
                else -> R.color.purple
            }
        } else {
            when (code) {
                1 -> R.color.green_2
                2 -> R.color.black
                3 -> R.color.blue
                4 -> R.color.white
                else -> R.color.yellow
            }
        }
    }

    private fun getSmallIconResId(completed: Boolean, code: Int): Int {
        return if (completed) {
            when (code) {
                1 -> R.drawable.ic_trophy_white
                2 -> R.drawable.ic_trophy_white
                3 -> R.drawable.ic_trophy_yellow
                4 -> R.drawable.ic_trophy_white
                else -> R.drawable.ic_trophy_yellow
            }
        } else {
            when (code) {
                1 -> R.drawable.ic_drink_holo_dark
                2 -> R.drawable.ic_drink_holo_dark
                3 -> R.drawable.ic_drink_holo_dark
                4 -> R.drawable.ic_drink_blue
                else -> R.drawable.ic_drink_blue
            }
        }
    }

    private fun getOnNotificationClickedPendingIntent(context: Context): PendingIntent {
        val intentNotification = Intent(context, MainActivity::class.java)
        intentNotification.action = NotificationClicked.action
        intentNotification.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        intentNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

        return PendingIntent.getActivity(
            context,
            NotificationClicked.code,
            intentNotification,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnNotificationDeletedPendingIntent(context: Context): PendingIntent {
        val intentDeleted = Intent(context, AlarmReceiver::class.java)
        intentDeleted.action = NotificationRemoved.action
        intentDeleted.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)

        return PendingIntent.getBroadcast(
            context,
            NotificationRemoved.code,
            intentDeleted,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnSkipPendingIntent(context: Context): PendingIntent {
        val intentSkip = Intent(context, AlarmReceiver::class.java)
        intentSkip.action = SkipDrink.action
        intentSkip.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)

        return PendingIntent.getBroadcast(
            context,
            SkipDrink.code,
            intentSkip,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnDrinkPendingIntent(context: Context): PendingIntent {
        val intentDrink = Intent(context, AlarmReceiver::class.java)
        intentDrink.action = Drink.action
        intentDrink.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)

        return PendingIntent.getBroadcast(
            context,
            Drink.code,
            intentDrink,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}