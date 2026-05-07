package com.eahm.drinkwaterapp

import android.app.Application
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.di.startDI
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import org.koin.core.context.GlobalContext

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationService.setup(this)

        startDI(this@BaseApplication)

        GlobalContext.get().getAll<AppInitializer>().forEach { it.initialize() }
    }
}
