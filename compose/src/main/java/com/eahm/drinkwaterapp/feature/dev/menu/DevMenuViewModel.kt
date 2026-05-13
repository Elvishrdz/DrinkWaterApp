package com.eahm.drinkwaterapp.feature.dev.menu

import androidx.lifecycle.ViewModel
import com.eahm.drinkwaterapp.core.config.AppInfoService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DevMenuViewModel(
    appInfoService: AppInfoService,
) : ViewModel() {

    val state: StateFlow<DevMenuState> = MutableStateFlow(
        DevMenuState(
            appVersion = appInfoService.versionName,
            buildVariant = appInfoService.buildVariant,
        )
    )
}
