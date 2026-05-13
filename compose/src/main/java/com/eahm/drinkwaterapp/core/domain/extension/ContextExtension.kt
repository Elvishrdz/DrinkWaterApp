package com.eahm.drinkwaterapp.core.domain.extension

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.eahm.drinkwaterapp.R

fun Context.openSystemAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
    )
}

internal val Context.canShowNotifications: Boolean
    get() = let {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            PackageManager.PERMISSION_GRANTED // for VERSION.SDK_INT < TIRAMISU
        }

        return permission == PackageManager.PERMISSION_GRANTED
    }

internal val Context.canScheduleExactAlarms: Boolean
    get() = let {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

fun Context.startTextSendIntent(
    shareText: String
) {
    val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val chooser = Intent.createChooser(
        fallbackIntent,
        getString(R.string.text_share_title),
    )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(chooser)
}

fun Context.startImageSendIntent(
    imageUri: Uri,
    shareText: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(
        shareIntent,
        getString(R.string.text_share_title),
    )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(chooser)
}
