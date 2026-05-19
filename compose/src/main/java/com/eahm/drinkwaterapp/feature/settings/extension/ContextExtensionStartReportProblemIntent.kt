package com.eahm.drinkwaterapp.feature.settings.extension

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.feature.settings.data.SupportEmail

internal fun Context.startReportProblemIntent() {
    val dataValue = StringBuilder().apply {
        val mailData = "mailto:${SupportEmail.email}"

        val appName = getString(R.string.app_name)
        val subjectValue = getString(R.string.problem_report_subject, appName)
        val subjectData = "subject=$subjectValue"

        val bodyValue = getString(R.string.problem_report_body, appName)
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
