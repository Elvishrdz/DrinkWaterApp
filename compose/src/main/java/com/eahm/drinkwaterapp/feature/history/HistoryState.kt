package com.eahm.drinkwaterapp.feature.history

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDateTime

internal sealed interface HistoryState {

    data object Loading : HistoryState

    @Immutable
    data class Data(
        val date: LocalDateTime,
        val historyRecords: ImmutableList<HourGroup>,
        val goalMl: Float,
        val consumedMl: Float,
        val isToday: Boolean,
    ) : HistoryState
}
