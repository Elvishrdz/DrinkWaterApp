package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.eahm.drinkwaterapp.core.config.PrivacyPolicyTermsOfServiceConfig

internal fun Context.openPrivacyPolicyAndTermsOfUse() {
    val intent = Intent(
        Intent.ACTION_VIEW,
        PrivacyPolicyTermsOfServiceConfig.LEGAL_URL.toUri(),
    )

    startActivity(intent)
}
