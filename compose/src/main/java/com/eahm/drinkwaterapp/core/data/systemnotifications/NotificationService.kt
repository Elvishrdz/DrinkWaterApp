package com.eahm.drinkwaterapp.core.data.systemnotifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eahm.drinkwaterapp.MainActivity
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.initialAmountPerDrink
import com.eahm.drinkwaterapp.core.data.systemnotifications.channel.DrinkNotificationChannel
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.ui.awesometexts.MessageCategory
import com.eahm.drinkwaterapp.ui.awesometexts.NotificationBigTextMessages
import com.eahm.drinkwaterapp.ui.awesometexts.NotificationTextMessages
import com.eahm.drinkwaterapp.ui.awesometexts.NotificationTitleMessages
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object NotificationService {

    private const val LOG_NOTIFICATIONS = "LOG_NOTIFICATIONS"

    private val titleMessages = NotificationTitleMessages()
    private val textMessages = NotificationTextMessages()
    private val bigTextMessages = NotificationBigTextMessages()

    fun setup(context: Context) {
        DrinkNotificationChannel.createChannel(context)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun showDrinkNotification(
        context: Context,
        hour: Int?,
        minute: Int?,
    ) {
        // TODO: check if exists, update messages based on:
        //  the status (is this notification visible for too long?
        // time (is midnight? close to end hours? or early morning?
        // goal amount, display how much
        if (!context.canShowNotifications) return

        val koin = GlobalContext.get()
        val prefsStorage = koin.get<PrefsStorage>()

        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())

        val startTime = prefsStorage.getStartTime().firstOrNull()
        val endTime = prefsStorage.getEndTime().firstOrNull()

        val category = when {
            startTime != null && startTime.hour == hour && startTime.minute == minute
                -> MessageCategory.Start

            endTime != null && endTime.hour == hour && endTime.minute == minute
                -> MessageCategory.End

            else ->
                MessageCategory.ActiveHours(
                    hour = hour ?: currentTime.hour,
                )
        }

        //val title = awesomeTexts.getTitle(3, context)
        //val text = awesomeTexts.getText(3, context)
        //val bigText = awesomeTexts.getBigText(4, context)
        val title = titleMessages.getTitle(category, context)

        val text = if (Random.nextInt(11) > 5)
            bigTextMessages.getBigText(category, context)
        else
            textMessages.getText(category, context)

        val notificationAmountPerDrinkMl = prefsStorage.getAmountPerDrink().firstOrNull()
            ?: initialAmountPerDrink

        DrinkNotification.showNotification(
            context = context,
            title = title,
            text = text,
            bigText = null,
            amountPerDrinkMl = notificationAmountPerDrinkMl,
        )
    }

    fun cancelDrinkNotification(context: Context) {
        DrinkNotification.cancelNotification(context)
    }

    @OptIn(ExperimentalTime::class)
    fun showReportNotification(
        CHANNEL_ID: String,
        NOTIFICATION_ID: Int,
        context: Context,
        title: String?,
        text: String?,
        bigText: String?
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intentNotification: Intent =
            Intent(context, MainActivity::class.java)
        intentNotification.setAction("ON_NOTIFICATION_CLICKED")
        intentNotification.putExtra("NOTI_ID", NOTIFICATION_ID)
        intentNotification.putExtra("IS_FROM_NOTIFICATION", true)
        intentNotification.putExtra("SHOW_REPORT", true)
        intentNotification.putExtra("IS_LAST_NOTIFICATION", true)

        val timestamp = Clock.System.now().toEpochMilliseconds()
        intentNotification.putExtra("TIMESTAMP", timestamp)

        intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent: PendingIntent? = PendingIntent.getActivity(
            context,
            0,
            intentNotification,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val notiColor: Int
        val notiSmallIcon: Int
        val notiLargeIcon: Bitmap? =
            BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_app_2)

        // TODO: if (SettingsClass().isGoalReached()) {
        if (false) {
            val random = Random(7).nextInt(1, 5)

            when (random) {
                1 -> {
                    notiColor = context.getResources().getColor(R.color.white)
                    notiSmallIcon = R.drawable.icon_trophie_yellow
                }

                else -> {
                    notiColor = context.getResources().getColor(R.color.yellow)
                    notiSmallIcon = R.drawable.icon_trophie_white
                }
            }
        } else {
            val random = Random(7).nextInt(1, 5)

            when (random) {
                1 -> {
                    notiColor = context.getResources().getColor(R.color.colorAccent)
                    notiSmallIcon = R.drawable.icon_report_analytics
                }

                2 -> {
                    notiColor = context.getResources().getColor(R.color.green)
                    notiSmallIcon = R.drawable.icon_report_analytics
                }

                else -> {
                    notiColor = context.getResources().getColor(R.color.purple)
                    notiSmallIcon = R.drawable.icon_report_analytics
                }
            }
        }

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ) //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
            .setSmallIcon(notiSmallIcon)
            .setLargeIcon(notiLargeIcon)
            .setColor(notiColor)
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(bigText)
            ) //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //.setOnlyAlertOnce(true)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        }

        //Uri uriAudio = Uri.parse("android.resource://"+ context.getPackageName() + "/" + R.raw.app_notification_sound);
        val arrayVibrate = longArrayOf(1000, 1000)

        //builder.setSound(uriAudio);
        builder.setVibrate(arrayVibrate)

        //builder.setLights(Color.RED, 3000, 3000);
        val notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }


    fun showReportNotificationBigPicture(
        CHANNEL_ID: String,
        NOTIFICATION_ID: Int,
        context: Context,
        title: String?,
        text: String?
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intentNotification: Intent =
            Intent(context, MainActivity::class.java)
        intentNotification.setAction("ON_NOTIFICATION_CLICKED")
        intentNotification.putExtra("NOTI_ID", NOTIFICATION_ID)
        intentNotification.putExtra("IS_FROM_NOTIFICATION", true)
        intentNotification.putExtra("SHOW_REPORT", true)
        intentNotification.putExtra("IS_LAST_NOTIFICATION", true)
        intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent: PendingIntent? = PendingIntent.getActivity(
            context,
            0,
            intentNotification,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            ) //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
                .setSmallIcon(R.drawable.icon_report_analytics)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.getResources(),
                        R.mipmap.ic_launcher
                    )
                )
                .setColor(context.getResources().getColor(R.color.blue_light))
                .setColorized(true)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER) //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //.setOnlyAlertOnce(true)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val bitmap: Bitmap? =
                BitmapFactory.decodeResource(context.getResources(), R.drawable.design_welcome_top)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
            )
        }

        //Uri uriAudio = Uri.parse("android.resource://"+ context.getPackageName() + "/" + R.raw.app_notification_sound);
        val arrayVibrate = longArrayOf(1000, 1000)

        //builder.setSound(uriAudio);
        builder.setVibrate(arrayVibrate)

        //builder.setLights(Color.RED, 3000, 3000);
        val notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun playAudio(context: Context, audioResourceID: Int) {
        val APP_NOTIFICATION_SOUND: Int = R.raw.app_notification_sound

        val name = context.getResources().getResourceName(audioResourceID)
        Log.i(
            LOG_NOTIFICATIONS,
            "ID NOMBRE; " + name
        )

        if (name == null || !name.startsWith(context.getPackageName())) {
            // id is not an id used by a layout element.
            return
        }

        val am: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val vib: Vibrator =
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        when (am.getRingerMode()) {
            AudioManager.RINGER_MODE_SILENT -> Log.i("MyApp", "Silent mode")
            AudioManager.RINGER_MODE_VIBRATE -> {
                Log.i("MyApp", "Vibrate mode")
                vib.vibrate(400)
            }

            AudioManager.RINGER_MODE_NORMAL -> {
                Log.i("MyApp", "Normal mode")

                //  MediaPlayer.create(DriverMapActivity.this, soundID).start();
                val mediaPlayer: MediaPlayer = MediaPlayer.create(context, audioResourceID)
                vib.vibrate(400)
                mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                    override fun onCompletion(mp: MediaPlayer?) {
                        mediaPlayer.release()
                    }
                })
                mediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                    override fun onError(
                        mp: MediaPlayer?,
                        what: Int,
                        extra: Int
                    ): Boolean {
                        try {
                            mediaPlayer.release()
                        } catch (ignore: Exception) {
                        }

                        return false
                    }
                })
            }
        }
    } //endregion
}
