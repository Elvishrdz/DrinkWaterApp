package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context

internal val Context.appPlayStoreUrl: String
    get() = let {
        val appPackageName = packageName
        return "https://play.google.com/store/apps/details?id=$appPackageName"
    }
