package com.eahm.drinkwaterapp.feature.dev.alarms

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AlarmsViewModel : ViewModel() {

    private val _state = MutableStateFlow<AlarmsState>(AlarmsState.Loading)
    val state: StateFlow<AlarmsState> = _state

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    fun onAction(action: AlarmsAction) {
        // handled as the feature grows
    }
}
