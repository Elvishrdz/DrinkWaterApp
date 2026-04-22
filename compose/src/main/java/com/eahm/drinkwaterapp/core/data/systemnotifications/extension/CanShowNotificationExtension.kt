package com.eahm.drinkwaterapp.core.data.systemnotifications.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

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
