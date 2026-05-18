package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

internal class AlarmsV2ViewModel(
    private val application: Application,
    private val drinkAlarmManagerV2: DrinkAlarmManagerV2,
) : ViewModel() {

    private val _state = MutableStateFlow<AlarmsV2State>(AlarmsV2State.Loading)
    val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        startTicker()
    }

    fun onAction(action: AlarmsV2Action) {
        when (action) {
            AlarmsV2Action.StartAlarms -> startAlarms()
            AlarmsV2Action.StopAlarms -> stopAlarms()
            AlarmsV2Action.Refresh -> refreshState()
            AlarmsV2Action.OpenExactPermissionSettings -> ExactAlarmPermission.openSettings(
                application
            )
        }
    }

    private fun startAlarms() {
        viewModelScope.launch {
            drinkAlarmManagerV2.restoreAlarm(source = "dev-menu")
            refreshState()
        }
    }

    private fun stopAlarms() {
        drinkAlarmManagerV2.stopAlarm()
        refreshState()
    }

    private fun refreshState() {
        val now = Calendar.getInstance()
        val currentTime = "%02d:%02d:%02d".format(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        val exactActive =
            AlarmSchedulerV3.isExactAlarmActive(application, DrinkAlarmManagerV2.EXACT_ALARM_ID)
        val inexactActive =
            AlarmSchedulerV3.isInexactAlarmActive(application, DrinkAlarmManagerV2.INEXACT_ALARM_ID)
        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val withinActiveHours = isWithinActiveHours(now)
        val nextSlot = computeNextSlot(now)

        _state.update {
            AlarmsV2State.Data(
                exactAlarmActive = exactActive,
                inexactAlarmActive = inexactActive,
                exactPermissionGranted = permissionGranted,
                isWithinActiveHours = withinActiveHours,
                nextSlot = nextSlot,
                currentTime = currentTime,
            )
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                refreshState()
                delay(1_000)
            }
        }
    }

    private fun isWithinActiveHours(now: Calendar): Boolean {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        return hour in 6 until 18
    }

    private fun computeNextSlot(now: Calendar): String {
        val slots = listOf(Pair(10, 0), Pair(13, 0), Pair(16, 0))
        val nowHour = now.get(Calendar.HOUR_OF_DAY)
        val nowMinute = now.get(Calendar.MINUTE)
        val next = slots.firstOrNull { (h, m) ->
            h > nowHour || (h == nowHour && m > nowMinute)
        }
        return if (next != null) {
            "%02d:%02d".format(next.first, next.second)
        } else {
            "%02d:%02d (tomorrow)".format(slots.first().first, slots.first().second)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
