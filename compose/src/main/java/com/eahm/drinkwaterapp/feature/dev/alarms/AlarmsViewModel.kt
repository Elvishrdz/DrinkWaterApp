package com.eahm.drinkwaterapp.feature.dev.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

internal class AlarmsViewModel(
    private val drinkAlarmManager: DrinkAlarmManager,
) : ViewModel() {

    val state = drinkAlarmManager.state.map {
        AlarmsState.Data(
            hasNotificationPermissions = it.hasNotificationPermissions,
            isAlarmActive = it.isAlarmActive,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AlarmsState.Loading,
        )

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    fun onAction(action: AlarmsAction) {
        when (action) {
            AlarmsAction.OnResumeCalled -> onResumeCalled()
        }
    }

    private fun onResumeCalled() = drinkAlarmManager.onResume()
}
