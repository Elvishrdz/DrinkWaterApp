package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal fun Context.openPlayStoreUrl() {
    val intent = Intent(Intent.ACTION_VIEW, appPlayStoreUrl.toUri())
    startActivity(intent)
}
