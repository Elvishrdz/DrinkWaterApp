package com.eahm.drinkwaterapp.feature.dev.historyinject

import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class InjectEntry(
    val id: String = Uuid.random().toString(),
    val date: LocalDate? = null,
    val hour: Int = 8,
    val minute: Int = 0,
    val amountText: String = "",
)

sealed interface HistoryInjectDevState {
    data object Loading : HistoryInjectDevState
    data class Data(
        val entries: List<InjectEntry> = listOf(InjectEntry()),
        val clearByDay: LocalDate? = null,
        val insertResult: String = "",
    ) : HistoryInjectDevState
}
