package com.eahm.drinkwaterapp.feature.history

import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal sealed interface HistoryState {

    data object Loading : HistoryState

    data class Data(
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
    ) : HistoryState
}
