package com.eahm.drinkwaterapp.feature.dev.exactalarm

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
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV2
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

internal const val TEST_ALARM_ID = 200
internal const val INEXACT_THRESHOLD_MS = 10_000L

internal class ExactAlarmDevViewModel(
    private val application: Application,
) : ViewModel() {

    private val _isPermissionGranted = MutableStateFlow(
        ExactAlarmPermission.isGranted(application)
    )
    private val _lastPermissionEvent = MutableStateFlow<String?>(null)
    private val _isTestAlarmActive = MutableStateFlow(
        AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)
    )
    private val _scheduledAsExact = MutableStateFlow<Boolean?>(null)
    private val _countdownMs = MutableStateFlow<Long?>(null)

    // Default: current time + 1 minute so it's ready to use immediately
    private val _selectedTime = MutableStateFlow(
        Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            TimeSelected(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
        }
    )

    private var countdownJob: Job? = null

    // Nest two combines to stay within the 5-flow typed overload limit
    private val _permissionPair = combine(
        _isPermissionGranted,
        _lastPermissionEvent,
    ) { granted, event -> Pair(granted, event) }

    val state = combine(
        _permissionPair,
        _isTestAlarmActive,
        _scheduledAsExact,
        _countdownMs,
        _selectedTime,
    ) { (isPermissionGranted, lastPermissionEvent), isTestAlarmActive, scheduledAsExact, countdownMs, selectedTime ->
        ExactAlarmDevState.Data(
            isPermissionGranted = isPermissionGranted,
            isTestAlarmActive = isTestAlarmActive,
            scheduledAsExact = scheduledAsExact,
            selectedTime = selectedTime,
            countdownMs = countdownMs,
            lastPermissionEvent = lastPermissionEvent,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ExactAlarmDevState.Loading,
    )

    // Receives ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED from the system
    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
            ) {
                onPermissionStateChanged()
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

    fun onAction(action: ExactAlarmDevAction) {
        when (action) {
            ExactAlarmDevAction.OpenPermissionSettings -> openSettings()
            ExactAlarmDevAction.ScheduleTestAlarm -> scheduleTestAlarm()
            ExactAlarmDevAction.CancelTestAlarm -> cancelTestAlarm()
            ExactAlarmDevAction.RefreshPermissionState -> refreshPermission()
            is ExactAlarmDevAction.OnTimeSelected -> _selectedTime.update { action.time }
        }
    }

    private fun openSettings() {
        ExactAlarmPermission.openSettings(application)
    }

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

    private fun refreshPermission() {
        _isPermissionGranted.update { ExactAlarmPermission.isGranted(application) }
        val alarmActive = AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)
        _isTestAlarmActive.update { alarmActive }
        if (alarmActive && countdownJob?.isActive != true) {
            startCountdownTicker()
        }
    }

    /**
     * Called when the system broadcasts ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.
     * Updates the permission state and re-schedules the active alarm so it uses the
     * correct alarm type (exact ↔ inexact) reflecting the new permission.
     */
    private fun onPermissionStateChanged() {
        val previousPermission = _isPermissionGranted.value
        val newPermission = ExactAlarmPermission.isGranted(application)
        _isPermissionGranted.update { newPermission }

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
            rescheduleAlarm(newPermission)
        }
    }

    private fun rescheduleAlarm(nowExact: Boolean) {
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
