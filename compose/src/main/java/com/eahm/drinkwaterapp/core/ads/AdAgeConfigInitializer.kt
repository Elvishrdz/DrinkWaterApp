package com.eahm.drinkwaterapp.core.ads

import com.eahm.drinkwaterapp.ads.AdAgeConfigurator
import com.eahm.drinkwaterapp.age.api.AdTreatment
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Keeps AdMob's age-appropriate request configuration in sync with the resolved age signal.
 *
 * Defaults to [AdAgeConfigurator.applyStandard] (no minor tags) — matching the plan's decision that
 * non-covered users get normal ads — and tightens to TFUA/TFCD only once we actually know the user
 * is a minor. This is safe because ad surfaces sit behind the age gate, so by the time a covered
 * minor can see an ad their treatment has already been applied.
 */
internal class AdAgeConfigInitializer(
    private val ageRepository: AgeRepository,
) : AppInitializer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun initialize() {
        AdAgeConfigurator.applyStandard()

        scope.launch {
            ageRepository.ageSignals.collect { signals ->
                when (signals?.adTreatment) {
                    AdTreatment.CHILD_DIRECTED -> AdAgeConfigurator.applyChildDirected()
                    AdTreatment.UNDER_AGE_OF_CONSENT -> AdAgeConfigurator.applyUnderAgeOfConsent()
                    AdTreatment.STANDARD, null -> AdAgeConfigurator.applyStandard()
                }
            }
        }
    }
}
