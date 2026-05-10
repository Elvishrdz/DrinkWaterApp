package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.BaseApplication
import com.eahm.drinkwaterapp.core.database.di.databaseModule
import com.eahm.drinkwaterapp.feature.drink.activeHoursObserverModule
import com.eahm.drinkwaterapp.feature.drink.clockTickObserverModule
import com.eahm.drinkwaterapp.feature.drink.dateTimeZoneChangedObserverModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal fun startDI(application: BaseApplication) {
    startKoin {
        androidContext(application)
        modules(
            dateTimeZoneChangedObserverModule,
            clockTickObserverModule,
            activeHoursObserverModule,
            databaseModule,
            dataModule,
            featureModule,
        )
    }
}
