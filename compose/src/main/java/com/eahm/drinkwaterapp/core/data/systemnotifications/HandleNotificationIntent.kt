package com.eahm.drinkwaterapp.core.data.systemnotifications

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.eahm.drinkwaterapp.ads.BuildConfig
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationClicked
import com.eahm.drinkwaterapp.core.data.systemnotifications.model.NotificationIntentOrigin
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.DrinkNotification.EXTRA_NOTIFICATION_ID
import org.koin.core.context.GlobalContext

fun Context.handleNotificationIntentOnCreate(intent: Intent) =
    handleNotificationIntent(intent, NotificationIntentOrigin.OnCreate)

fun Context.handleNotificationIntentOnNewIntent(intent: Intent) =
    handleNotificationIntent(intent, NotificationIntentOrigin.OnNewIntent)

private fun Context.handleNotificationIntent(
    intent: Intent,
    origin: NotificationIntentOrigin,
) {
    val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

    when (intent.action) {
        NotificationClicked.action -> {
            if (notificationId == DrinkNotification.NOTIFICATION_ID) {
                intent.action = null
                GlobalContext.get().get<TrackingService>().trackNotificationOpened()
                // TODO: redirect to history

                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "¡hello! $origin", Toast.LENGTH_SHORT).show()
                }
            }
        }

        else -> {
            /* ignore */
        }
    }
}
