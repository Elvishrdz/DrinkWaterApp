package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.feature.settings.data.SupportEmail

internal fun Context.startFeatureRequestIntent() {
    val dataValue = StringBuilder().apply {
        val mailData = "mailto:${SupportEmail.email}"

        val uniqueCode = "FR200"
        val appName = getString(R.string.app_name)
        val subjectValue = getString(R.string.feature_request_subject, appName)
        val subjectData = "subject=$uniqueCode: $subjectValue"

        val bodyValue = getString(R.string.feature_request_body, appName)
        val bodyData = "body=$bodyValue"

        append(mailData)
        append("?")
        append(subjectData)
        append("&")
        append(bodyData)
    }.toString()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = dataValue.toUri()
    }
    startActivity(intent)
}
