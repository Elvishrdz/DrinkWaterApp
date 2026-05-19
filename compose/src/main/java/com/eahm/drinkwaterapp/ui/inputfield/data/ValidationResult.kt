package com.eahm.drinkwaterapp.ui.inputfield.data

import android.content.Context
import androidx.annotation.StringRes

data class ValidationResult(
    val isValid: Boolean,
    @StringRes val messageRes: Int = 0,
    val formatArg: String = ""
) {
    fun resolveMessage(context: Context): String {
        if (messageRes == 0) return ""
        return if (formatArg.isEmpty()) context.getString(messageRes)
        else context.getString(messageRes, formatArg)
    }
}
