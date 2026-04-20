package com.eahm.drinkwaterapp

import android.app.Application
import com.eahm.drinkwaterapp.core.data.datastore.dataStoreModule
import com.eahm.drinkwaterapp.core.data.datastore.prefsStorageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseApplication)
            modules(
                dataStoreModule,
                prefsStorageModule,
            )
        }
    }
}