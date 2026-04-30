package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.core.data.confetti.confettiModule
import com.eahm.drinkwaterapp.core.data.datastore.dataStoreModule
import com.eahm.drinkwaterapp.core.data.datastore.prefsStorageModule
import com.eahm.drinkwaterapp.core.data.drinkalarm.drinkAlarmModule
import com.eahm.drinkwaterapp.core.data.goalreachedobserver.goalReachedObserverModule
import org.koin.dsl.module

internal val dataModule = module {
    includes(confettiModule)
    includes(dataStoreModule)
    includes(prefsStorageModule)
    includes(drinkAlarmModule)
    includes(goalReachedObserverModule)
}
