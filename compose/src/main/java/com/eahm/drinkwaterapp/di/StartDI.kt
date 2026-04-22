package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.BaseApplication
import com.eahm.drinkwaterapp.core.data.datastore.dataStoreModule
import com.eahm.drinkwaterapp.core.data.datastore.prefsStorageModule
import com.eahm.drinkwaterapp.core.data.drinkalarm.drinkAlarmModule
import com.eahm.drinkwaterapp.core.database.di.androidDatabaseModule
import com.eahm.drinkwaterapp.feature.drink.featureDrinkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal fun startDI(application: BaseApplication) {
    startKoin {
        androidContext(application)
        modules(
            drinkAlarmModule,
            androidDatabaseModule,
            dataStoreModule,
            prefsStorageModule,
            featureModule,
        )
    }
}

private val featureModule = module {
    includes(featureDrinkModule)
}
