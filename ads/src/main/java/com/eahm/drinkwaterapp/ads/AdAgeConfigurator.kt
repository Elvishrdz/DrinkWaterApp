package com.eahm.drinkwaterapp.ads

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/**
 * Applies age-appropriate ad serving via AdMob's [RequestConfiguration]. Driven by the resolved age
 * signal (Play Age Signals API or the declared fallback). Ad-compliance law has two thresholds, so
 * both AdMob tags are set:
 *
 * - [applyChildDirected] → TFCD (`setTagForChildDirectedTreatment`), COPPA / under-13.
 * - [applyUnderAgeOfConsent] → TFUA (`setTagForUnderAgeOfConsent`), 13–17.
 * - [applyStandard] → no minor tag (18+ or no constraint).
 *
 * This only configures *how* ads are served; the age value is never used for targeting, profiling,
 * or analytics.
 */
object AdAgeConfigurator {

    /** Under-13 audience → child-directed treatment (also implies under age of consent). */
    fun applyChildDirected() = apply(childDirected = true, underAgeOfConsent = true)

    /** 13–17 audience → tagged as under the age of consent. */
    fun applyUnderAgeOfConsent() = apply(childDirected = false, underAgeOfConsent = true)

    /** 18+ or no known constraint → no minor tags. */
    fun applyStandard() = apply(childDirected = false, underAgeOfConsent = false)

    private fun apply(childDirected: Boolean, underAgeOfConsent: Boolean) {
        val configuration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                if (childDirected) {
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                } else {
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
                },
            )
            .setTagForUnderAgeOfConsent(
                if (underAgeOfConsent) {
                    RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                } else {
                    RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED
                },
            )
            .build()
        MobileAds.setRequestConfiguration(configuration)
    }
}
