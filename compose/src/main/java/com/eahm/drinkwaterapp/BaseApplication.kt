package com.eahm.drinkwaterapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.di.startDI
import com.eahm.drinkwaterapp.system.foreground.AppLifecycleObserver

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationService.setup(this)

        startDI(this@BaseApplication)

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}
