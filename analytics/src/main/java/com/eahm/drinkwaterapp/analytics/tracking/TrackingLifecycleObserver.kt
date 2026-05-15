package com.eahm.drinkwaterapp.analytics.tracking

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Observes the app process lifecycle and flushes queued Mixpanel events to the server
 * whenever the app moves to the background.
 *
 * Uses [ProcessLifecycleOwner] so no Activity reference is needed.
 * Note: [ProcessLifecycleOwner] never delivers ON_DESTROY; [onStop] is the
 * last reliable callback before the app leaves the foreground.
 */
class TrackingLifecycleObserver(
    private val trackingService: TrackingService,
) : DefaultLifecycleObserver {

    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        trackingService.flush()
    }
}
