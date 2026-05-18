package com.eahm.drinkwaterapp.feature.dev.alarmsv2

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
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.domain.extension.openSystemAppSettings
import com.eahm.drinkwaterapp.feature.dev.alarmsv2.AlarmsV2ViewModel.Companion.ON_TIME_THRESHOLD_MS
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV2
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import com.eahm.drinkwaterapp.system.alarm.ExactAlarmPermission
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Any delay value above this threshold (12 h) is considered a next-day rollover.
 * It is used to detect when a slot has just passed: the delay drops to near-zero,
 * the alarm fires, and then [AlarmSchedulerV3.getMillisUntilXxxTrigger] rolls over
 * to >24 h. If the previous measurement was below 12 h and the current one is
 * above, the slot has fired and we enter the post-fire display state.
 */
private const val SAME_DAY_MAX_MS = 12 * 3600 * 1000L

internal class AlarmsV2ViewModel(
    private val application: Application,
    private val drinkAlarmManagerV2: DrinkAlarmManagerV2,
) : ViewModel() {

    private val _events = Channel<AlarmsV2Event>(Channel.BUFFERED)
    val events: Flow<AlarmsV2Event> = _events.receiveAsFlow()

    private val _state = MutableStateFlow<AlarmsV2State>(AlarmsV2State.Loading)
    val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    // V2 exact alarm countdown tracking
    private var lastExactSlot: Pair<Int, Int>? = null
    private var lastExactDelayMs: Long? = null
    private var exactPostFire = false

    // V2 inexact alarm countdown tracking
    private var lastInexactSlot: Pair<Int, Int>? = null
    private var lastInexactDelayMs: Long? = null
    private var inexactPostFire = false

    // Test alarm (AlarmSchedulerV2) state
    private var lastKnownExactPermission = ExactAlarmPermission.isGranted(application)
    private val _lastPermissionEvent = MutableStateFlow<String?>(null)
    private val _scheduledAsExact = MutableStateFlow<Boolean?>(null)
    private val _selectedTestTime = MutableStateFlow(
        Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            TimeSelected(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE))
        }
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

            AlarmsV2Action.CallOnResume -> callOnResume()
            AlarmsV2Action.SetupV1Alarm -> refreshState() // V1 alarm is disabled; trigger a refresh
            AlarmsV2Action.StopV1Alarm -> refreshState()  // V1 alarm is disabled; trigger a refresh
            AlarmsV2Action.NotificationPermissionGranted -> refreshState()
            AlarmsV2Action.OpenAppPermissionSettings -> application.openSystemAppSettings()
            AlarmsV2Action.ScheduleTestAlarm -> scheduleTestAlarm()
            AlarmsV2Action.CancelTestAlarm -> cancelTestAlarm()
            is AlarmsV2Action.OnTestAlarmTimeSelected -> _selectedTestTime.update { action.time }
        }
    }

    // region V2 alarm actions

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

    // endregion

    // region V1 / legacy actions

    private fun callOnResume() {
        viewModelScope.launch {
            _events.send(AlarmsV2Event.OnResumeCalled)
        }
    }

    // endregion

    // region Test alarm (AlarmSchedulerV2) actions

    private fun scheduleTestAlarm() {
        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val time = _selectedTestTime.value
        AlarmSchedulerV2.scheduleAlarm(application, TEST_ALARM_ID, time.hour, time.minute, 0)
        _scheduledAsExact.update { permissionGranted }
        refreshState()
    }

    private fun cancelTestAlarm() {
        AlarmSchedulerV2.cancelAlarm(application, TEST_ALARM_ID)
        _scheduledAsExact.update { null }
        refreshState()
    }

    private fun onExactPermissionStateChanged() {
        val previousPermission = lastKnownExactPermission
        val newPermission = ExactAlarmPermission.isGranted(application)
        lastKnownExactPermission = newPermission

        val isTestAlarmActive = AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)

        val eventDescription = buildString {
            append(if (newPermission) "GRANTED" else "REVOKED")
            if (isTestAlarmActive && newPermission != previousPermission) {
                val from = if (previousPermission) "exact" else "inexact"
                val to = if (newPermission) "exact" else "inexact"
                append(" — alarm rescheduled $from → $to")
            }
        }
        _lastPermissionEvent.update { eventDescription }

        if (isTestAlarmActive && newPermission != previousPermission) {
            rescheduleTestAlarm(newPermission)
        }

        refreshState()
    }

    private fun rescheduleTestAlarm(nowExact: Boolean) {
        val time = _selectedTestTime.value
        AlarmSchedulerV2.cancelAlarm(application, TEST_ALARM_ID)
        AlarmSchedulerV2.scheduleAlarm(application, TEST_ALARM_ID, time.hour, time.minute, 0)
        _scheduledAsExact.update { nowExact }
    }

    // endregion

    // region State refresh

    private fun refreshState() {
        val now = Calendar.getInstance()
        val currentTime = "%02d:%02d:%02d".format(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )

        val permissionGranted = ExactAlarmPermission.isGranted(application)
        val withinActiveHours = isWithinActiveHours(now)
        val nextSlot = computeNextSlot(now)

        // --- V2 Exact alarm countdown ---
        val exactActive =
            AlarmSchedulerV3.isExactAlarmActive(application, DrinkAlarmManagerV2.EXACT_ALARM_ID)
        val inexactActive =
            AlarmSchedulerV3.isInexactAlarmActive(application, DrinkAlarmManagerV2.INEXACT_ALARM_ID)

        val exactCountdownMs: Long?
        val exactScheduledAt: String?
        if (!exactActive) {
            val stale =
                AlarmSchedulerV3.getStoredExactSlot(application, DrinkAlarmManagerV2.EXACT_ALARM_ID)
            if (stale != null) {
                AlarmSchedulerV3.clearStoredExactSlot(
                    application,
                    DrinkAlarmManagerV2.EXACT_ALARM_ID
                )
            }
            lastExactSlot = null
            lastExactDelayMs = null
            exactPostFire = false
            exactCountdownMs = null
            exactScheduledAt = null
        } else {
            val storedExact =
                AlarmSchedulerV3.getStoredExactSlot(application, DrinkAlarmManagerV2.EXACT_ALARM_ID)
            if (storedExact != null) {
                exactScheduledAt = "%02d:%02d".format(storedExact.first, storedExact.second)
                val delayMs = AlarmSchedulerV3.getMillisUntilExactTrigger(
                    application,
                    DrinkAlarmManagerV2.EXACT_ALARM_ID
                ) ?: 0L
                if (storedExact != lastExactSlot) {
                    lastExactSlot = storedExact
                    lastExactDelayMs = delayMs
                    exactPostFire = false
                } else if (!exactPostFire) {
                    val last = lastExactDelayMs
                    if (last != null && last < SAME_DAY_MAX_MS && delayMs > SAME_DAY_MAX_MS) {
                        exactPostFire = true
                    }
                    lastExactDelayMs = delayMs
                }
                exactCountdownMs = if (exactPostFire) 0L else delayMs
            } else {
                lastExactSlot = null
                lastExactDelayMs = null
                exactPostFire = false
                exactCountdownMs = null
                exactScheduledAt = null
            }
        }

        // --- V2 Inexact alarm countdown ---
        val storedInexact =
            AlarmSchedulerV3.getStoredInexactSlot(application, DrinkAlarmManagerV2.INEXACT_ALARM_ID)
        val inexactCountdownMs: Long?
        val inexactScheduledAt: String?
        if (storedInexact != null) {
            inexactScheduledAt = "%02d:%02d".format(storedInexact.first, storedInexact.second)
            val delayMs = AlarmSchedulerV3.getMillisUntilInexactTrigger(
                application,
                DrinkAlarmManagerV2.INEXACT_ALARM_ID
            ) ?: 0L
            if (storedInexact != lastInexactSlot) {
                lastInexactSlot = storedInexact
                lastInexactDelayMs = delayMs
                inexactPostFire = false
            } else if (!inexactPostFire) {
                val last = lastInexactDelayMs
                if (last != null && last < SAME_DAY_MAX_MS && delayMs > SAME_DAY_MAX_MS) {
                    inexactPostFire = true
                }
                lastInexactDelayMs = delayMs
            }
            inexactCountdownMs = if (inexactPostFire) 0L else delayMs
        } else {
            lastInexactSlot = null
            lastInexactDelayMs = null
            inexactPostFire = false
            inexactCountdownMs = null
            inexactScheduledAt = null
        }

        // --- V2 Inexact receipt (early / late / on time) ---
        val receipt = AlarmSchedulerV3.getInexactReceipt(
            application,
            DrinkAlarmManagerV2.INEXACT_ALARM_ID
        )
        val inexactEarlyMessage = receipt?.let { (deltaMs, scheduledH, scheduledM) ->
            val timeStr = "%02d:%02d".format(scheduledH, scheduledM)
            when {
                deltaMs > ON_TIME_THRESHOLD_MS ->
                    "received ${formatDuration(deltaMs)} earlier (scheduled at $timeStr)"

                deltaMs < -ON_TIME_THRESHOLD_MS ->
                    "received ${formatDuration(-deltaMs)} later (scheduled at $timeStr)"

                else ->
                    "received at $timeStr (on time)"
            }
        }

        // --- V1 alarm state (legacy) ---
        val v1AlarmActive = AlarmSchedulerV2.isAlarmActive(application, V1_ALARM_ID)
        val v1AlarmStorageInfo =
            if (v1AlarmActive) AlarmSchedulerV2.getAlarmStorageInfo(application, V1_ALARM_ID)
            else null

        // --- Notification permission ---
        val hasNotificationPermissions = application.canShowNotifications

        // --- Test alarm (AlarmSchedulerV2) ---
        val isTestAlarmActive = AlarmSchedulerV2.isAlarmActive(application, TEST_ALARM_ID)
        val testCountdownMs =
            if (isTestAlarmActive) AlarmSchedulerV2.getTimeUntilNextTrigger(
                application,
                TEST_ALARM_ID
            )
            else null

        _state.update {
            AlarmsV2State.Data(
                currentTime = currentTime,
                exactAlarmActive = exactActive,
                exactCountdownMs = exactCountdownMs,
                exactScheduledAt = exactScheduledAt,
                inexactAlarmActive = inexactActive,
                inexactCountdownMs = inexactCountdownMs,
                inexactScheduledAt = inexactScheduledAt,
                inexactEarlyMessage = inexactEarlyMessage,
                v1AlarmActive = v1AlarmActive,
                v1AlarmStorageInfo = v1AlarmStorageInfo,
                exactPermissionGranted = permissionGranted,
                hasNotificationPermissions = hasNotificationPermissions,
                isWithinActiveHours = withinActiveHours,
                nextSlot = nextSlot,
                isTestAlarmActive = isTestAlarmActive,
                scheduledAsExact = _scheduledAsExact.value,
                testCountdownMs = testCountdownMs,
                selectedTestTime = _selectedTestTime.value,
                lastPermissionEvent = _lastPermissionEvent.value,
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

    // endregion

    // region Helpers

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

    /**
     * Converts a duration in milliseconds to a human-readable string, scaling the
     * unit to whatever makes the value most readable:
     *  < 1 s  → "312ms"
     *  < 1 min → "45s" or "45s 312ms"
     *  < 1 h  → "3m 05s"
     *  ≥ 1 h  → "2h 03m"
     */
    private fun formatDuration(ms: Long): String = when {
        ms < 1_000 -> "${ms}ms"
        ms < 60_000 -> {
            val s = ms / 1_000
            val rem = ms % 1_000
            if (rem > 0) "${s}s ${rem}ms" else "${s}s"
        }

        ms < 3_600_000 -> {
            val m = ms / 60_000
            val s = (ms % 60_000) / 1_000
            if (s > 0) "${m}m %02ds".format(s) else "${m}m"
        }

        else -> {
            val h = ms / 3_600_000
            val m = (ms % 3_600_000) / 60_000
            if (m > 0) "${h}h %02dm".format(m) else "${h}h"
        }
    }

    // endregion

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.unregisterReceiver(permissionReceiver)
        }
    }

    companion object {
        /** Request code for DrinkAlarmManager's V1 alarm (DrinkAlarmManager.DRINK_ALARM_ID). */
        private const val V1_ALARM_ID = 127

        /** Request code for the AlarmSchedulerV2 test alarm (AlarmsViewModel.TEST_ALARM_ID). */
        private const val TEST_ALARM_ID = 200

        /**
         * Receipt delta within ±[ON_TIME_THRESHOLD_MS] is displayed as "on time".
         * Anything outside this window is shown as "X earlier" or "X later".
         */
        private const val ON_TIME_THRESHOLD_MS = 30_000L
    }
}
