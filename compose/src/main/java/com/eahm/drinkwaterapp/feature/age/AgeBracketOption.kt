package com.eahm.drinkwaterapp.feature.age

import androidx.annotation.StringRes
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.age.api.AdTreatment

/**
 * Predefined age options shown on the collection and Settings screens. Three buckets matching the
 * two legal ad thresholds — finer granularity would map to the same AdMob tag, so it adds nothing.
 */
internal enum class AgeBracketOption(
    @param:StringRes val labelRes: Int,
    val treatment: AdTreatment,
) {
    UNDER_13(R.string.age_option_under_13, AdTreatment.CHILD_DIRECTED),
    TEEN_13_17(R.string.age_option_13_17, AdTreatment.UNDER_AGE_OF_CONSENT),
    ADULT_18_PLUS(R.string.age_option_18_plus, AdTreatment.STANDARD),
    ;

    companion object {
        /** Returns the option matching a stored treatment, for pre-selecting in the editor. */
        fun fromTreatment(treatment: AdTreatment?): AgeBracketOption? =
            entries.firstOrNull { it.treatment == treatment }
    }
}
