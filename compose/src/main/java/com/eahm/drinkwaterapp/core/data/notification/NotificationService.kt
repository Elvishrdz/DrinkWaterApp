package com.eahm.drinkwaterapp.core.data.notification
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.BitmapFactory
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.os.Build
//import android.os.Vibrator
//import com.eahm.drinkwaterapp.MainActivity
//import com.eahm.drinkwaterapp.R
//import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
//
//class NotificationService {
//
//    //endregion VARIABLES
//    //region NOTIFICATIONS
//    private fun createNotificationChannel(context: Context, channelId: String) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = context.getString(R.string.app_name)
//            val description = context.getString(R.string.channel_description)
//
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel(channelId, name, importance)
//            channel.description = description
//
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            val notificationManager = context.getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    fun showDrinkNotification(
//        channelId: String,
//        notificationId: Int,
//        context: Context,
//        title: String?,
//        text: String?,
//        bigText: String?
//    ) {
//        if (androidx.core.app.ActivityCompat.checkSelfPermission(
//                context,
//                android.Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        createNotificationChannel(context, channelId)
//
//        //region THIS NOTIFICATION ON CLICK
//        val intentNotification = Intent(context, MainActivity::class.java)
//        intentNotification.action = "ON_NOTIFICATION_CLICKED"
//
//        intentNotification.putExtra("NOTI_ID", notificationId)
//        intentNotification.putExtra("IS_FROM_NOTIFICATION", true)
//        intentNotification.putExtra("IS_DRINK_TIME", true)
//
//        intentNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//        val pendingIntent: PendingIntent? = PendingIntent.getActivity(
//            context,
//            0,
//            intentNotification,
//            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
//        )
//
//        //endregion THIS NOTIFICATION ON CLICK
//
//        //region NOTIFICACION CLOSED
//        val intentDeleted = Intent(context, AlarmReceiver::class.java)
//        intentDeleted.action = "ON_DELETED"
//        intentDeleted.putExtra("NOTI_ID", notificationId)
//        intentDeleted.putExtra("CODE", "CLOSED")
//        val pendingIntentDeleted: PendingIntent? = PendingIntent.getBroadcast(
//            context,
//            CODE_CLOSED,
//            intentDeleted,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        //endregion NOTIFICACION CLOSED
//
//        //region NOTIFICATION BUTTON SKIP
//        val intentSkip = Intent(context, AlarmReceiver::class.java)
//        intentSkip.action = "ON_SKIP"
//        intentSkip.putExtra("NOTI_ID", notificationId)
//        intentSkip.putExtra("CODE", "SKIP")
//        val pendingIntentSkip: PendingIntent? = PendingIntent.getBroadcast(
//            context,
//            CODE_SKIP,
//            intentSkip,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        //endregion NOTIFICATION BUTTON SKIP
//
//        //region NOTIFICATION BUTTON DRINK
//        val intentDrink = Intent(context, AlarmReceiver::class.java)
//        intentDrink.action = "ON_DRINK"
//        intentDrink.putExtra("NOTI_ID", notificationId)
//        intentDrink.putExtra("CODE", "DRINK")
//        val pendingIntentDrink: PendingIntent? = PendingIntent.getBroadcast(
//            context,
//            CODE_DRINK,
//            intentDrink,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        //endregion NOTIFICATION BUTTON DRINK
//
//        //region CUSTOMIZE NOTIFICATION
//        val notiColor: kotlin.Int
//        val notiSmallIcon: kotlin.Int
//        var notiLargeIcon: android.graphics.Bitmap? =
//            BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher)
//
//        if (SettingsClass().isGoalReached()) {
//            notiLargeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_app_2)
//
//            val random: Int = Utils().getRandomNumber(1, 5)
//            when (random) {
//                1 -> {
//                    notiColor = context.getResources().getColor(R.color.purple)
//                    notiSmallIcon = R.drawable.ic_trophy_white
//                }
//
//                2 -> {
//                    notiColor = context.getResources().getColor(R.color.blue)
//                    notiSmallIcon = R.drawable.ic_trophy_white
//                }
//
//                3 -> {
//                    notiColor = context.getResources().getColor(R.color.black)
//                    notiSmallIcon = R.drawable.ic_trophy_yellow
//                }
//
//                4 -> {
//                    notiColor = context.getResources().getColor(R.color.grey_3)
//                    notiSmallIcon = R.drawable.ic_trophy_white
//                }
//
//                else -> {
//                    notiColor = context.getResources().getColor(R.color.purple)
//                    notiSmallIcon = R.drawable.ic_trophy_yellow
//                }
//            }
//        } else {
//            val random: kotlin.Int = Utils().getRandomNumber(1, 5)
//            when (random) {
//                1 -> {
//                    notiColor = context.getResources().getColor(R.color.green_2)
//                    notiSmallIcon = R.drawable.ic_drink_holo_dark
//                }
//
//                2 -> {
//                    notiColor = context.getResources().getColor(R.color.black)
//                    notiSmallIcon = R.drawable.ic_drink_holo_dark
//                }
//
//                3 -> {
//                    notiColor = context.getResources().getColor(R.color.blue)
//                    notiSmallIcon = R.drawable.ic_drink_holo_dark
//                }
//
//                4 -> {
//                    notiColor = context.getResources().getColor(R.color.white)
//                    notiSmallIcon = R.drawable.ic_drink_blue
//                }
//
//                else -> {
//                    notiColor = context.getResources().getColor(R.color.yellow)
//                    notiSmallIcon = R.drawable.ic_drink_blue
//                }
//            }
//        }
//
//        //endregion CUSTOMIZE NOTIFICATION
//
//        //region CREATE THE NOTIFICATION
//        val builder = androidx.core.app.NotificationCompat.Builder(
//            context,
//            channelId
//        ) //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
//            .setSmallIcon(notiSmallIcon)
//            .setLargeIcon(notiLargeIcon)
//            .setColor(notiColor)
//            .setColorized(true)
//            .setContentTitle(title)
//            .setContentText(text)
//            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(bigText))
//            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
//            .setContentIntent(pendingIntent)
//            .addAction(
//                0,
//                context.getResources().getString(R.string.text_skip),
//                pendingIntentSkip
//            ) //R.drawable.icon_skip
//            .addAction(
//                0,
//                context.getResources().getString(R.string.text_drink),
//                pendingIntentDrink
//            ) //R.drawable.icon_drink
//            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER) //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            //.setOnlyAlertOnce(true)
//            .setAutoCancel(true)
//            .setDeleteIntent(pendingIntentDeleted)
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_LARGE)
//        }
//
//        val uriAudio =
//            android.net.Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.app_notification_sound)
//        val arrayVibrate = kotlin.longArrayOf(1000, 1000)
//
//        builder.setSound(uriAudio)
//        builder.setVibrate(arrayVibrate)
//        builder.setLights(android.graphics.Color.RED, 3000, 3000)
//
//        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
//
//        // notificationId is a unique int for each notification that you must define
//        //notificationManager.notify(NOTIFICATION_ID, builder.build());
//        notificationManager.notify(notificationId, builder.build())
//
//        //endregion CREATE THE NOTIFICATION
//    }
//
//
//    fun showReportNotification(
//        CHANNEL_ID: String,
//        NOTIFICATION_ID: kotlin.Int,
//        context: Context,
//        title: String?,
//        text: String?,
//        bigText: String?
//    ) {
//        if (androidx.core.app.ActivityCompat.checkSelfPermission(
//                context,
//                android.Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        createNotificationChannel(context, CHANNEL_ID)
//
//        //region THIS NOTIFICATION ON CLICK
//        val intentNotification: Intent =
//            Intent(context, MainActivity::class.java)
//        intentNotification.setAction("ON_NOTIFICATION_CLICKED")
//        intentNotification.putExtra("NOTI_ID", NOTIFICATION_ID)
//        intentNotification.putExtra("IS_FROM_NOTIFICATION", true)
//        intentNotification.putExtra("SHOW_REPORT", true)
//        intentNotification.putExtra("IS_LAST_NOTIFICATION", true)
//        intentNotification.putExtra("TIMESTAMP", Utils().getCurrentTimestamp())
//        intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        val pendingIntent: PendingIntent? = PendingIntent.getActivity(
//            context,
//            0,
//            intentNotification,
//            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
//        )
//
//        //endregion THIS NOTIFICATION ON CLICK
//
//        //region CUSTOMIZE NOTIFICATION
//        val notiColor: Int
//        val notiSmallIcon: kotlin.Int
//        val notiLargeIcon: android.graphics.Bitmap? =
//            BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_app_2)
//
//        if (SettingsClass().isGoalReached()) {
//            val random: kotlin.Int = Utils().getRandomNumber(1, 2)
//            when (random) {
//                1 -> {
//                    notiColor = context.getResources().getColor(R.color.white)
//                    notiSmallIcon = R.drawable.icon_trophie_yellow
//                }
//
//                else -> {
//                    notiColor = context.getResources().getColor(R.color.yellow)
//                    notiSmallIcon = R.drawable.icon_trophie_white
//                }
//            }
//        } else {
//            val random: kotlin.Int = Utils().getRandomNumber(1, 3)
//            when (random) {
//                1 -> {
//                    notiColor = context.getResources().getColor(R.color.colorAccent)
//                    notiSmallIcon = R.drawable.icon_report_analytics
//                }
//
//                2 -> {
//                    notiColor = context.getResources().getColor(R.color.green)
//                    notiSmallIcon = R.drawable.icon_report_analytics
//                }
//
//                else -> {
//                    notiColor = context.getResources().getColor(R.color.purple)
//                    notiSmallIcon = R.drawable.icon_report_analytics
//                }
//            }
//        }
//
//        //endregion CUSTOMIZE NOTIFICATION
//
//        //region CREATE THE NOTIFICATION
//        val builder = androidx.core.app.NotificationCompat.Builder(
//            context,
//            CHANNEL_ID
//        ) //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
//            .setSmallIcon(notiSmallIcon)
//            .setLargeIcon(notiLargeIcon)
//            .setColor(notiColor)
//            .setColorized(true)
//            .setContentTitle(title)
//            .setContentText(text)
//            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
//            .setStyle(
//                androidx.core.app.NotificationCompat.BigTextStyle().bigText(bigText)
//            ) //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            //.setOnlyAlertOnce(true)
//            .setAutoCancel(true)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_LARGE)
//        }
//
//        //Uri uriAudio = Uri.parse("android.resource://"+ context.getPackageName() + "/" + R.raw.app_notification_sound);
//        val arrayVibrate = kotlin.longArrayOf(1000, 1000)
//
//        //builder.setSound(uriAudio);
//        builder.setVibrate(arrayVibrate)
//
//        //builder.setLights(Color.RED, 3000, 3000);
//        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
//
//        // notificationId is a unique int for each notification that you must define
//        notificationManager.notify(NOTIFICATION_ID, builder.build())
//
//        //endregion CREATE THE NOTIFICATION
//    }
//
//
//    fun showReportNotificationBigPicture(
//        CHANNEL_ID: String,
//        NOTIFICATION_ID: kotlin.Int,
//        context: Context,
//        title: String?,
//        text: String?
//    ) {
//        if (androidx.core.app.ActivityCompat.checkSelfPermission(
//                context,
//                android.Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        createNotificationChannel(context, CHANNEL_ID)
//
//        //region THIS NOTIFICATION ON CLICK
//        val intentNotification: Intent =
//            Intent(context, MainActivity::class.java)
//        intentNotification.setAction("ON_NOTIFICATION_CLICKED")
//        intentNotification.putExtra("NOTI_ID", NOTIFICATION_ID)
//        intentNotification.putExtra("IS_FROM_NOTIFICATION", true)
//        intentNotification.putExtra("SHOW_REPORT", true)
//        intentNotification.putExtra("IS_LAST_NOTIFICATION", true)
//        intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        val pendingIntent: PendingIntent? = PendingIntent.getActivity(
//            context,
//            0,
//            intentNotification,
//            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
//        )
//
//        //endregion THIS NOTIFICATION ON CLICK
//
//        //region CREATE THE NOTIFICATION
//        val builder: androidx.core.app.NotificationCompat.Builder =
//            androidx.core.app.NotificationCompat.Builder(
//                context,
//                CHANNEL_ID
//            ) //.setSmallIcon(R.mipmap.ic_launcher, 0) // pequeño circulo a la derecha del icono de la notificacion
//                .setSmallIcon(R.drawable.icon_report_analytics)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        context.getResources(),
//                        R.mipmap.ic_launcher
//                    )
//                )
//                .setColor(context.getResources().getColor(R.color.com_facebook_blue))
//                .setColorized(true)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER) //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                //.setOnlyAlertOnce(true)
//                .setAutoCancel(true)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_LARGE)
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            val bitmap: android.graphics.Bitmap? =
//                BitmapFactory.decodeResource(context.getResources(), R.drawable.design_welcome_top)
//            builder.setStyle(
//                androidx.core.app.NotificationCompat.BigPictureStyle()
//                    .bigPicture(bitmap)
//            )
//        }
//
//        //Uri uriAudio = Uri.parse("android.resource://"+ context.getPackageName() + "/" + R.raw.app_notification_sound);
//        val arrayVibrate = kotlin.longArrayOf(1000, 1000)
//
//        //builder.setSound(uriAudio);
//        builder.setVibrate(arrayVibrate)
//
//        //builder.setLights(Color.RED, 3000, 3000);
//        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
//
//        // notificationId is a unique int for each notification that you must define
//        notificationManager.notify(NOTIFICATION_ID, builder.build())
//
//        //endregion CREATE THE NOTIFICATION
//    }
//
//
//    //endregion NOTIFICATIONS
//    //region AUDIOS MANAGEMENT
//    private fun playAudio(context: Context, audioResourceID: kotlin.Int) {
//        val APP_NOTIFICATION_SOUND: kotlin.Int = R.raw.app_notification_sound
//
//        val name = context.getResources().getResourceName(audioResourceID)
//        android.util.Log.i(
//            com.eahm.drinkwaterapp.core.data.notification.NotificationService.Companion.LOG_NOTIFICATIONS,
//            "ID NOMBRE; " + name
//        )
//
//        if (name == null || !name.startsWith(context.getPackageName())) {
//            // id is not an id used by a layout element.
//            return
//        }
//
//        val am: AudioManager =
//            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val vib: Vibrator =
//            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//        when (am.getRingerMode()) {
//            AudioManager.RINGER_MODE_SILENT -> android.util.Log.i("MyApp", "Silent mode")
//            AudioManager.RINGER_MODE_VIBRATE -> {
//                android.util.Log.i("MyApp", "Vibrate mode")
//                vib.vibrate(400)
//            }
//
//            AudioManager.RINGER_MODE_NORMAL -> {
//                android.util.Log.i("MyApp", "Normal mode")
//
//                //  MediaPlayer.create(DriverMapActivity.this, soundID).start();
//                val mediaPlayer: MediaPlayer = MediaPlayer.create(context, audioResourceID)
//                vib.vibrate(400)
//                mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
//                    override fun onCompletion(mp: MediaPlayer?) {
//                        mediaPlayer.release()
//                    }
//                })
//                mediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
//                    override fun onError(
//                        mp: MediaPlayer?,
//                        what: kotlin.Int,
//                        extra: kotlin.Int
//                    ): kotlin.Boolean {
//                        try {
//                            mediaPlayer.release()
//                        } catch (ignore: java.lang.Exception) {
//                        }
//
//                        return false
//                    }
//                })
//            }
//        }
//    } //endregion
//
//    companion object {
//        //region VARIABLES
//        private const val LOG_NOTIFICATIONS = "LOG_NOTIFICATIONS"
//        private val CODE_SKIP = 123
//        private val CODE_DRINK = 456
//        private val CODE_CLOSED = 789
//    }
//}
