package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val username: String?,
        val currentStreak: Int,
        val quickDrinkPresets: List<DrinkPreset>,
        val allDrinkPresets: List<DrinkPreset>,
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
        val todayLogs: List<DrinkLogEntry>,
        val drinkStats: DrinkStats?,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DrinkState
}

data class DrinkStats(
    val goalAmount: Float,
    val drinkAmount: Float,
    val goalReached: Boolean,
) {
    val remainingAmount = (goalAmount - drinkAmount).coerceAtLeast(0f)
}

data class DrinkExtras(
    val username: String?,
    val quickDrinkPresets: List<DrinkPreset>,
    val allDrinkPresets: List<DrinkPreset>,
)
