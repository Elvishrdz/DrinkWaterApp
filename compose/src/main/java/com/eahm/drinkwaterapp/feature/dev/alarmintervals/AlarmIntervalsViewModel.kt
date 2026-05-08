package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AlarmIntervalsViewModel : ViewModel() {

    private val _state = MutableStateFlow<AlarmIntervalsState>(AlarmIntervalsState.Loading)
    val state: StateFlow<AlarmIntervalsState> = _state

    private val _events = Channel<AlarmIntervalsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmIntervalsEvent> = _events.receiveAsFlow()

    fun onAction(action: AlarmIntervalsAction) {
        // handled as the feature grows
    }
}
