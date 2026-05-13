package com.eahm.drinkwaterapp.system.service.goalcelebration.extension

import android.content.Intent
import android.os.Build
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle

internal fun Intent.getSerializableCelebrationStyle(name: String): CelebrationStyle? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, CelebrationStyle::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(name) as? CelebrationStyle
    }
}
