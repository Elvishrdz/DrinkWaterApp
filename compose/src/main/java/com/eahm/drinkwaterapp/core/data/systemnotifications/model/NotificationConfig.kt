package com.eahm.drinkwaterapp.core.data.systemnotifications.model

import android.graphics.Bitmap

data class NotificationConfig(
    val color: Int,
    val smallIcon: Int,
    var largeIcon: Bitmap,
)