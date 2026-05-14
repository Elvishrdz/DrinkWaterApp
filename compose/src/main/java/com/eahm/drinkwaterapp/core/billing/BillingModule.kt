package com.eahm.drinkwaterapp.core.billing

import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val billingModule = module {
    singleOf(::PurchaseManager) bind AppInitializer::class
}
