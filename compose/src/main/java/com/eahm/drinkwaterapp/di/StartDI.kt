package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.BaseApplication
import com.eahm.drinkwaterapp.core.data.confetti.confettiModule
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
            androidDatabaseModule,
            dataModule,
            featureModule,
        )
    }
}

private val dataModule = module {
    includes(confettiModule)
    includes(dataStoreModule)
    includes(prefsStorageModule)
    includes(drinkAlarmModule)

}

private val featureModule = module {
    includes(featureDrinkModule)
}
