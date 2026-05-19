package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

internal fun Context.openPrivacyPolicyAndTermsOfUse() {
    val privacyPolicyAndTermsOfUseUrl =
        "https://www.freeprivacypolicy.com/privacy/view/04c015dc119a00b1ec8a21df46a9bfb1"

    val intent = Intent(
        Intent.ACTION_VIEW,
        privacyPolicyAndTermsOfUseUrl.toUri(),
    )

    startActivity(intent)
}
