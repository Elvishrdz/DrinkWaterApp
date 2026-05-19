package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import com.eahm.drinkwaterapp.R

internal fun Context.startAppShareIntent() {
    val appName = getString(R.string.app_name)

    val appPackageName = packageName
    val appPlayStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"

    val messageText = getString(
        R.string.settings_share_text,
        appName,
        appPlayStoreUrl,
    )

    val target = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, messageText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(
        target,
        null,
    )

    startActivity(shareIntent)
}
