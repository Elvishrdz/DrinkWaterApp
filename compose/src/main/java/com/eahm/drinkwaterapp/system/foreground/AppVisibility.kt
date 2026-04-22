package com.eahm.drinkwaterapp.system.foreground

import kotlinx.coroutines.flow.MutableStateFlow

object AppVisibility {

    val isAppInForeground = MutableStateFlow(false)

    fun setAppInForeground() {
        isAppInForeground.value = true
    }

    fun setAppInBackground() {
        isAppInForeground.value = false
    }
}
