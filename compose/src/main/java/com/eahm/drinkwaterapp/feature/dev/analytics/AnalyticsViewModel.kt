package com.eahm.drinkwaterapp.feature.dev.analytics

import androidx.lifecycle.ViewModel
import com.eahm.drinkwaterapp.core.config.AppInfoService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class AnalyticsViewModel(
    private val appInfoService: AppInfoService,
) : ViewModel() {

    private val _events = Channel<AnalyticsEvent>(Channel.BUFFERED)
    val events: Flow<AnalyticsEvent> = _events.receiveAsFlow()

    private val _state = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    val state: StateFlow<AnalyticsState> = _state

    init {
        _state.update {
            AnalyticsState.Data(
                versionName = appInfoService.versionName,
                buildVariant = appInfoService.buildVariant,
            )
        }
    }

    fun onAction(action: AnalyticsAction) {
        // handled as the feature grows
    }
}
