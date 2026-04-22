package com.eahm.drinkwaterapp.core.data.systemnotifications.model

import android.graphics.Bitmap

data class NotificationConfig(
    val color: Int,
    val smallIconResId: Int,
    var largeIcon: Bitmap?,
)