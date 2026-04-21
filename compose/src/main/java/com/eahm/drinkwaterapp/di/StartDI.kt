package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.BaseApplication
import com.eahm.drinkwaterapp.core.data.datastore.dataStoreModule
import com.eahm.drinkwaterapp.core.data.datastore.prefsStorageModule
import com.eahm.drinkwaterapp.core.database.di.androidDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal fun startDI(application: BaseApplication) {
    startKoin {
        androidContext(application)
        modules(
            androidDatabaseModule,
            dataStoreModule,
            prefsStorageModule,
        )
    }
}
