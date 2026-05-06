package com.eahm.drinkwaterapp.core.domain.extension

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings

fun Context.openSystemAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
    )
}
