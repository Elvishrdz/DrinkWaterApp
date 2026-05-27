package com.eahm.drinkwaterapp.core.config

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

object PrivacyPolicyTermsOfServiceConfig {

    const val LEGAL_URL =
        "https://drink-water-app-869b5.web.app"

    const val LEGACY_URL =
        "https://www.freeprivacypolicy.com/privacy/view/04c015dc119a00b1ec8a21df46a9bfb1"

    /** Currently-effective Privacy Policy date (UTC).
     *  Update this value whenever the policy changes to force re-acceptance. */
    val effectiveDate = LocalDate(
        year = 2026,
        month = Month.MAY,
        day = 26,
    )

    val CURRENT_POLICY_DATE: Long = effectiveDate.toEpochDays() * 24L * 60 * 60 * 1000
}
