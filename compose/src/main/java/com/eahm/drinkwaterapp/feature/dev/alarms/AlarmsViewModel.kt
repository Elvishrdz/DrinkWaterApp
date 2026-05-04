package com.eahm.drinkwaterapp.feature.dev.alarms

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.Settings
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
    private val application: Application,
    private val drinkAlarmManager: DrinkAlarmManager,
) : ViewModel() {

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

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    fun onAction(action: AlarmsAction) {
        when (action) {
            AlarmsAction.OnResumeCalled -> onResumeCalled()
            AlarmsAction.NotificationPermissionGranted -> updateState()
            AlarmsAction.OpenAppPermissionSettings -> openAppPermissionSettings()
            AlarmsAction.SetupAlarm -> setupAlarm()
            AlarmsAction.StopAlarm -> onStopAlarm()
        }
    }

    private fun onResumeCalled() = drinkAlarmManager.onResume()
    private fun updateState() = drinkAlarmManager.refresh()
    private fun openAppPermissionSettings() = application.openSystemAppSettings()
    private fun onStopAlarm() = drinkAlarmManager.stopAlarms()
    private fun setupAlarm() = drinkAlarmManager.testScheduleAlarm()
}

fun Context.openSystemAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = FLAG_ACTIVITY_NEW_TASK
        }
    )
}
