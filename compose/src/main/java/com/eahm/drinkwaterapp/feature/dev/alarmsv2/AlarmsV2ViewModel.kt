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

    private val _state = MutableStateFlow<AlarmsV2State>(AlarmsV2State.Loading)
    val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    // Exact alarm countdown tracking
    private var lastExactSlot: Pair<Int, Int>? = null
    private var lastExactDelayMs: Long? = null
    private var exactPostFire = false

    // Inexact alarm countdown tracking
    private var lastInexactSlot: Pair<Int, Int>? = null
    private var lastInexactDelayMs: Long? = null
    private var inexactPostFire = false

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

        // --- Exact alarm countdown ---
        // If the alarm is not active (permission revoked → Android cancelled it),
        // wipe any stale stored slot so it never feeds incorrect data downstream.
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
                    // New slot scheduled → reset post-fire state
                    lastExactSlot = storedExact
                    lastExactDelayMs = delayMs
                    exactPostFire = false
                } else if (!exactPostFire) {
                    val last = lastExactDelayMs
                    // If previous delay was within today's window and current jumped past midnight,
                    // the slot just fired — enter post-fire state.
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

        // --- Inexact alarm countdown ---
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

        // --- Inexact early receipt ---
        val earlyReceipt = AlarmSchedulerV3.getInexactEarlyReceipt(
            application,
            DrinkAlarmManagerV2.INEXACT_ALARM_ID
        )
        val inexactEarlyMessage = earlyReceipt?.let { (earlyMs, scheduledH, scheduledM) ->
            val timeStr = "%02d:%02d".format(scheduledH, scheduledM)
            "received ${formatDuration(earlyMs)} earlier (scheduled at $timeStr)"
        }

        _state.update {
            AlarmsV2State.Data(
                exactAlarmActive = exactActive,
                inexactAlarmActive = inexactActive,
                exactPermissionGranted = permissionGranted,
                isWithinActiveHours = withinActiveHours,
                nextSlot = nextSlot,
                currentTime = currentTime,
                exactCountdownMs = exactCountdownMs,
                exactScheduledAt = exactScheduledAt,
                inexactCountdownMs = inexactCountdownMs,
                inexactScheduledAt = inexactScheduledAt,
                inexactEarlyMessage = inexactEarlyMessage,
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

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
