package com.eahm.drinkwaterapp.feature.dev.alarms

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.domain.extension.openSystemAppSettings
import com.eahm.drinkwaterapp.feature.drink.ActiveHoursObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AlarmsViewModel(
    private val application: Application,
    private val drinkAlarmManager: DrinkAlarmManager,
    activeHoursObserver: ActiveHoursObserver,
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    val state = combine(
        drinkAlarmManager.state,
        activeHoursObserver.inActiveHours,
    ) { drinkAlarmState, inActiveHours ->
        AlarmsState.Data(
            canScheduleExactAlarms = drinkAlarmState.canScheduleExactAlarms,
            hasNotificationPermissions = drinkAlarmState.hasNotificationPermissions,
            isAlarmActive = drinkAlarmState.isAlarmActive,
            alarmStorageInfo = drinkAlarmState.alarmStorageInfo,
            inActiveHours = inActiveHours,
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
