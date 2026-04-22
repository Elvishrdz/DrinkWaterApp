package com.eahm.drinkwaterapp.system.foreground

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

internal class AppLifecycleObserver : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        AppVisibility.setAppInForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        AppVisibility.setAppInBackground()
    }
}
