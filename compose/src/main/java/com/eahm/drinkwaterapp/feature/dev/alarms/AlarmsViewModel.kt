package com.eahm.drinkwaterapp.feature.dev.alarms

import android.app.AlarmManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.domain.extension.openSystemAppSettings
import com.eahm.drinkwaterapp.feature.drink.ActiveHoursObserver
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV2
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

internal const val TEST_ALARM_ID = 200
internal const val INEXACT_THRESHOLD_MS = 10_000L

internal class AlarmsViewModel(
    private val application: Application,
    private val drinkAlarmManager: DrinkAlarmManager,
    activeHoursObserver: ActiveHoursObserver,
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val _events = Channel<AlarmsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmsEvent> = _events.receiveAsFlow()

    // --- Exact alarm test state ---
    private var lastKnownExactPermission = ExactAlarmPermission.isGranted(application)
    private val _lastPermissionEvent = MutableStateFlow<String?>(null)
    private val _isTestAlarmActive = MutableStateFlow(
        AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)
    )
    private val _scheduledAsExact = MutableStateFlow<Boolean?>(null)
    private val _countdownMs = MutableStateFlow<Long?>(null)
    private val _selectedTime = MutableStateFlow(
        Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            TimeSelected(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
        }
    )
    private var countdownJob: Job? = null

    private val _testAlarmDetails = combine(
        _scheduledAsExact,
        _countdownMs,
        _selectedTime,
    ) { scheduledAsExact, countdownMs, selectedTime ->
        Triple(scheduledAsExact, countdownMs, selectedTime)
    }

    val state = combine(
        drinkAlarmManager.state,
        activeHoursObserver.inActiveHours,
        _lastPermissionEvent,
        _isTestAlarmActive,
        _testAlarmDetails,
    ) { drinkAlarmState, inActiveHours, lastPermissionEvent, isTestAlarmActive, (scheduledAsExact, countdownMs, selectedTime) ->
        AlarmsState.Data(
            canScheduleExactAlarms = drinkAlarmState.canScheduleExactAlarms,
            hasNotificationPermissions = drinkAlarmState.hasNotificationPermissions,
            isAlarmActive = drinkAlarmState.isAlarmActive,
            alarmStorageInfo = drinkAlarmState.alarmStorageInfo,
            inActiveHours = inActiveHours,
            isTestAlarmActive = isTestAlarmActive,
            scheduledAsExact = scheduledAsExact,
            countdownMs = countdownMs,
            selectedTime = selectedTime,
            lastPermissionEvent = lastPermissionEvent,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AlarmsState.Loading,
        )

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
            ) {
                onExactPermissionStateChanged()
            }
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.registerReceiver(
                application,
                permissionReceiver,
                IntentFilter(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
    }

    fun onAction(action: AlarmsAction) {
        when (action) {
            AlarmsAction.OnResumeCalled -> onResumeCalled()
            AlarmsAction.NotificationPermissionGranted -> updateState()
            AlarmsAction.OpenAppPermissionSettings -> openAppPermissionSettings()
            AlarmsAction.SetupAlarm -> setupAlarm()
            AlarmsAction.StopAlarm -> onStopAlarm()
            AlarmsAction.ScheduleTestAlarm -> scheduleTestAlarm()
            AlarmsAction.CancelTestAlarm -> cancelTestAlarm()
            AlarmsAction.RefreshPermissionState -> refreshPermissionState()
            AlarmsAction.OpenExactAlarmPermissionSettings -> ExactAlarmPermission.openSettings(
                application
            )

            is AlarmsAction.OnTestAlarmTimeSelected -> _selectedTime.update { action.time }
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

    private fun scheduleTestAlarm() {
        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val time = _selectedTime.value
        AlarmSchedulerV2.scheduleAlarm(application, TEST_ALARM_ID, time.hour, time.minute, 0)
        _scheduledAsExact.update { permissionGranted }
        _isTestAlarmActive.update { true }
        startCountdownTicker()
    }

    private fun cancelTestAlarm() {
        countdownJob?.cancel()
        AlarmSchedulerV2.cancelAlarm(application, TEST_ALARM_ID)
        _isTestAlarmActive.update { false }
        _scheduledAsExact.update { null }
        _countdownMs.update { null }
    }

    private fun refreshPermissionState() {
        lastKnownExactPermission = ExactAlarmPermission.isGranted(application)
        drinkAlarmManager.update()
        val alarmActive = AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)
        _isTestAlarmActive.update { alarmActive }
        if (alarmActive && countdownJob?.isActive != true) {
            startCountdownTicker()
        }
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = AlarmSchedulerV2.getTimeUntilNextTrigger(application, TEST_ALARM_ID)
                _countdownMs.update { remaining }
                if (remaining == null || remaining <= 0) {
                    _isTestAlarmActive.update { false }
                    break
                }
                delay(1_000)
            }
        }
    }

    private fun onExactPermissionStateChanged() {
        val previousPermission = lastKnownExactPermission
        val newPermission = ExactAlarmPermission.isGranted(application)
        lastKnownExactPermission = newPermission
        drinkAlarmManager.update()

        val eventDescription = buildString {
            append(if (newPermission) "GRANTED" else "REVOKED")
            if (_isTestAlarmActive.value && newPermission != previousPermission) {
                val from = if (previousPermission) "exact" else "inexact"
                val to = if (newPermission) "exact" else "inexact"
                append(" — alarm rescheduled $from → $to")
            }
        }
        _lastPermissionEvent.update { eventDescription }

        if (_isTestAlarmActive.value && newPermission != previousPermission) {
            rescheduleTestAlarm(newPermission)
        }
    }

    private fun rescheduleTestAlarm(nowExact: Boolean) {
        val time = _selectedTime.value
        AlarmSchedulerV2.cancelAlarm(application, TEST_ALARM_ID)
        AlarmSchedulerV2.scheduleAlarm(application, TEST_ALARM_ID, time.hour, time.minute, 0)
        _scheduledAsExact.update { nowExact }
        startCountdownTicker()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.unregisterReceiver(permissionReceiver)
        }
    }
}
