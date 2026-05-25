package com.eahm.drinkwaterapp.core.config

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

object PrivacyPolicyConfig {
    /** Currently-effective Privacy Policy date (UTC).
     *  Update this value whenever the policy changes to force re-acceptance. */
    private val policyDate = LocalDate(
        year = 2026,
        month = Month.MAY,
        day = 25,
    )

    val CURRENT_POLICY_DATE: Long = policyDate.toEpochDays() * 24L * 60 * 60 * 1000
}
