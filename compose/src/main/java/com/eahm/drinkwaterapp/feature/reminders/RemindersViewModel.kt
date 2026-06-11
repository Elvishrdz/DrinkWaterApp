package com.eahm.drinkwaterapp.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingReminders.Companion.ReminderDaysSource
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val prefsStorage: PrefsStorage,
    private val alarmManager: DrinkAlarmManagerV2,
    private val trackingService: TrackingService,
) : ViewModel() {

    @Suppress("MagicNumber")
    val state = prefsStorage.getActiveDays()
        .map { RemindersState(activeDays = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RemindersState(),
        )

    @Suppress("MagicNumber")
    fun onEnableAllDays() {
        viewModelScope.launch {
            prefsStorage.setActiveDays(setOf(1, 2, 3, 4, 5, 6, 7))
            alarmManager.restoreAlarm(source = "enable_all_days")
            trackingService.trackReminderAllDaysEnabled(ReminderDaysSource.Reminders)
        }
    }

    fun onDayToggled(dayOfWeek: Int) {
        viewModelScope.launch {
            val current = state.value.activeDays
            val updated = if (dayOfWeek in current) {
                // Keep at least one day active
                if (current.size > 1) current - dayOfWeek else current
            } else {
                current + dayOfWeek
            }
            prefsStorage.setActiveDays(updated)
            alarmManager.restoreAlarm(source = "active_days_changed")
            if (updated != current) {
                trackingService.trackReminderDayToggled(
                    isoDay = dayOfWeek,
                    enabled = dayOfWeek in updated,
                    activeIsoDays = updated,
                    source = ReminderDaysSource.Reminders,
                )
            }
        }
    }
}
