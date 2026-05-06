package com.eahm.drinkwaterapp.feature.dev.alarms

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.domain.extension.openSystemAppSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AlarmsViewModel(
    private val application: Application,
    private val drinkAlarmManager: DrinkAlarmManager,
) : ViewModel() {

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    val state = drinkAlarmManager.state.map {
        AlarmsState.Data(
            hasNotificationPermissions = it.hasNotificationPermissions,
            isAlarmActive = it.isAlarmActive,
            alarmStorageInfo = it.alarmStorageInfo,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AlarmsState.Loading,
        )


    fun onAction(action: AlarmsAction) {
        when (action) {
            AlarmsAction.OnResumeCalled -> onResumeCalled()
            AlarmsAction.NotificationPermissionGranted -> updateState()
            AlarmsAction.OpenAppPermissionSettings -> openAppPermissionSettings()
            AlarmsAction.SetupAlarm -> setupAlarm()
            AlarmsAction.StopAlarm -> onStopAlarm()
        }
    }

    private fun onResumeCalled() {
        viewModelScope.launch {
            drinkAlarmManager.onResume()
            _events.send(AlarmsEvent.OnResumeCalled)
        }
    }

    private fun updateState() = drinkAlarmManager.update()
    private fun openAppPermissionSettings() = application.openSystemAppSettings()
    private fun onStopAlarm() = drinkAlarmManager.stopAlarm()
    private fun setupAlarm() = drinkAlarmManager.setupTestAlarm(AlarmSource.AlarmsDevMenu)
}
