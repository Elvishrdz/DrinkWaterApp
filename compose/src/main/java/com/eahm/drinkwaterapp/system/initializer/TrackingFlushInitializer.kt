package com.eahm.drinkwaterapp.system.initializer

import com.eahm.drinkwaterapp.analytics.tracking.TrackingLifecycleObserver

/**
 * Bridges [TrackingLifecycleObserver] (analytics module) into the [AppInitializer]
 * framework so it is registered with [ProcessLifecycleOwner] at app startup.
 */
internal class TrackingFlushInitializer(
    private val observer: TrackingLifecycleObserver,
) : AppInitializer {
    override fun initialize() {
        observer.initialize()
    }
}
