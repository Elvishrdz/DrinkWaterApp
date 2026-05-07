package com.eahm.drinkwaterapp.system.foreground

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.eahm.drinkwaterapp.system.initializer.AppInitializer

internal class AppLifecycleObserver : DefaultLifecycleObserver, AppInitializer {

    override fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        AppVisibility.setAppInForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        AppVisibility.setAppInBackground()
    }
}
