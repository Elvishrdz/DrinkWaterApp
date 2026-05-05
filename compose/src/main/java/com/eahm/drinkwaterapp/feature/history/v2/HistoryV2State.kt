package com.eahm.drinkwaterapp.feature.history.v2

import com.eahm.drinkwaterapp.ui.historylist.HourGroup

internal sealed interface HistoryV2State {

    data object Loading : HistoryV2State

    data class Data(
        val days: List<DayItem>,
        val selectedDayKey: String,
        val selectedDayLabel: String,
        val totalMl: Int,
        val drinkCount: Int,
        val goalMl: Int,
        val percent: Int,
        val records: List<HourGroup>,
        val canGoPrev: Boolean,
        val canGoNext: Boolean,
    ) : HistoryV2State
}

data class DayItem(
    val key: String,          // "2026-05-05"
    val weekday: String,      // "Tue"
    val dayNum: String,       // "5"
    val fullLabel: String,    // "Today, May 5"
    val isToday: Boolean,
    val isEnabled: Boolean,   // false for padding days outside the record range
    val goalMet: Boolean,     // TODO: wire real per-day totals
)
