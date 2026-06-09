package com.eahm.drinkwaterapp.age.internal

import com.eahm.drinkwaterapp.age.api.AgeDebugController
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider

/**
 * Wraps the real [AgeSignalsProvider] with an optional override used by the developer menu to
 * simulate API responses. When [override] is null (the default / production path) it delegates to
 * the real provider; when set, [resolve] returns the override instead of calling Play.
 */
internal class SwitchableAgeSignalsProvider(
    private val delegate: AgeSignalsProvider,
) : AgeSignalsProvider, AgeDebugController {

    @Volatile
    override var override: AgeResolution? = null
        private set

    override fun simulate(resolution: AgeResolution?) {
        override = resolution
    }

    override suspend fun resolve(): AgeResolution = override ?: delegate.resolve()
}
