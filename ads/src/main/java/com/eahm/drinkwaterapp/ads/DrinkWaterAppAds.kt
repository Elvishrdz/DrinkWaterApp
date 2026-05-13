package com.eahm.drinkwaterapp.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val drinkWaterAppAdsModule = module {
    singleOf(::DrinkWaterAppAds)
}

class DrinkWaterAppAds {

    val isReady = MutableStateFlow(false)

    suspend fun Context.initialize() = withContext(Dispatchers.IO) {
        MobileAds.initialize(this@initialize) { status ->
            isReady.value = status.adapterStatusMap.any {
                it.value.initializationState == AdapterStatus.State.READY
            }
        }
    }
}
