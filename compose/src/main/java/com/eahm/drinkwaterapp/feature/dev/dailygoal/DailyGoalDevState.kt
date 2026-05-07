package com.eahm.drinkwaterapp.feature.dev.dailygoal

import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.datetime.LocalDate

internal sealed interface DailyGoalDevState {
    data object Loading : DailyGoalDevState

    data class Data(
        val todayGoalMl: Float,
        val allRecords: List<DailyGoalEntity>,
        val selectedDate: LocalDate,
        val inputGoalMl: Float,
    ) : DailyGoalDevState
}
