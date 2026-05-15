package com.eahm.drinkwaterapp.analytics.tracking

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingServiceModule = module {
    singleOf(::MixpanelTrackingService) bind TrackingService::class
    singleOf(::TrackingLifecycleObserver)
}
