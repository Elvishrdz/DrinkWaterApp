package com.eahm.drinkwaterapp.core.domain.extension

import com.eahm.drinkwaterapp.core.presentation.UnitPreference

fun Float.sanitizeZeroDecimals(): String {
    return if (this % 1f == 0f) {
        this.toInt().toString()
    } else {
        "%.1f".format(this)
    }
}

fun Float.formatVolume(pref: UnitPreference): String = when (pref) {
    UnitPreference.ML -> "${sanitizeZeroDecimals()} mL"
    UnitPreference.AUTO_LITERS ->
        if (this >= 1000f) "${"%.1f".format(this / 1000f)} L"
        else "${sanitizeZeroDecimals()} mL"

    UnitPreference.FL_OZ -> "${"%.1f".format(this / 29.574f)} fl oz"
}
