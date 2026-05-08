package com.eahm.drinkwaterapp.core.data.systemnotifications.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.eahm.drinkwaterapp.MainActivity
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.systemnotifications.channel.DrinkNotificationChannel
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.Drink
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationClicked
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationConfig
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationRemoved
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.SkipDrink
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlin.random.Random

internal object DrinkNotification {
    const val NOTIFICATION_ID = 1
    const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
    const val EXTRA_CODE = "CODE"
    const val AMOUNT_PER_DRINK_ML = "AMOUNT_PER_DRINK_ML"

    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        title: String?,
        text: String?,
        bigText: String?,
        amountPerDrinkMl: Float,
    ) {
        if (!context.canShowNotifications) return

        val channelId = DrinkNotificationChannel.DRINK_CHANNEL_ID
        val notificationId = NOTIFICATION_ID

        val config = getNotificationConfig(context)

        val contentIntent = getOnNotificationClickedPendingIntent(context)
        val skipIntent = getOnSkipPendingIntent(context)
        val drinkIntent = getOnDrinkPendingIntent(context, amountPerDrinkMl)
        val deleteIntent = getOnNotificationDeletedPendingIntent(context)

        val builder = NotificationCompat.Builder(context, channelId)
            //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
            .setSmallIcon(config.smallIconResId)
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
                // TODO:context.resources.getString(R.string.text_drink),
                "+$amountPerDrinkMl",
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
            ("android.resource://" + context.packageName + "/" + R.raw.app_notification_sound).toUri()
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

        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, largeIconResId)
        }.getOrNull()

        return NotificationConfig(
            color = color,
            smallIconResId = smallIconResId,
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
        intentNotification.putExtra(EXTRA_CODE, NotificationClicked.code)
        intentNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

        return PendingIntent.getActivity(
            context,
            NotificationClicked.intentCode,
            intentNotification,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnNotificationDeletedPendingIntent(context: Context): PendingIntent {
        val intentDeleted = Intent(context, AlarmReceiver::class.java)
        intentDeleted.action = NotificationRemoved.action
        intentDeleted.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        intentDeleted.putExtra(EXTRA_CODE, NotificationRemoved.code)

        return PendingIntent.getBroadcast(
            context,
            NotificationRemoved.intentCode,
            intentDeleted,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnSkipPendingIntent(context: Context): PendingIntent {
        val intentSkip = Intent(context, AlarmReceiver::class.java)
        intentSkip.action = SkipDrink.action
        intentSkip.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        intentSkip.putExtra(EXTRA_CODE, SkipDrink.code)

        return PendingIntent.getBroadcast(
            context,
            SkipDrink.intentCode,
            intentSkip,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getOnDrinkPendingIntent(context: Context, amountPerDrinkMl: Float): PendingIntent {
        val intentDrink = Intent(context, AlarmReceiver::class.java)
        intentDrink.action = Drink.action
        intentDrink.putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        intentDrink.putExtra(EXTRA_CODE, Drink.code)
        intentDrink.putExtra(AMOUNT_PER_DRINK_ML, amountPerDrinkMl)

        return PendingIntent.getBroadcast(
            context,
            Drink.intentCode,
            intentDrink,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
