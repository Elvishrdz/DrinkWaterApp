package com.eahm.drinkwaterapp.core.config

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Configuration for the privacy policy and terms of service.
 *
 * Legacy web (deprecated): <href>https://www.freeprivacypolicy.com/privacy/view/04c015dc119a00b1ec8a21df46a9bfb1<>
 */
object PrivacyPolicyTermsOfServiceConfig {

    const val LEGAL_URL =
        "https://drink-water-app-869b5.web.app"

    /** Currently-effective Privacy Policy date (UTC).
     *  Update this value whenever the policy changes to force re-acceptance. */
    val effectiveDate = LocalDate(
        year = 2026,
        month = Month.JUNE,
        day = 20,
    )

    val CURRENT_POLICY_DATE: Long = effectiveDate.toEpochDays() * 24L * 60 * 60 * 1000
}
