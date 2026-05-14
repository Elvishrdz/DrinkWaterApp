package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.core.billing.billingModule
import com.eahm.drinkwaterapp.core.config.appInfoModule
import com.eahm.drinkwaterapp.core.data.confetti.confettiModule
import com.eahm.drinkwaterapp.core.data.datastore.dataStoreModule
import com.eahm.drinkwaterapp.core.data.datastore.prefsStorageModule
import com.eahm.drinkwaterapp.core.data.drinkalarm.drinkAlarmModule
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.whatToDoServiceModule
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.drinkStatsObserverModule
import com.eahm.drinkwaterapp.core.data.notificationpermission.notificationPermissionModule
import com.eahm.drinkwaterapp.core.data.streak.streakModule
import org.koin.dsl.module

internal val dataModule = module {
    includes(appInfoModule)
    includes(confettiModule)
    includes(dataStoreModule)
    includes(prefsStorageModule)
    includes(drinkAlarmModule)
    includes(whatToDoServiceModule)
    includes(drinkStatsObserverModule)
    includes(notificationPermissionModule)
    includes(streakModule)
    includes(billingModule)
}
