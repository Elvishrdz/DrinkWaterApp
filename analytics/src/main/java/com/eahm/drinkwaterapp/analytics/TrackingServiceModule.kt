package com.eahm.drinkwaterapp.analytics

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingServiceModule = module {
    singleOf(::MixpanelTrackingService) bind TrackingService::class
}
