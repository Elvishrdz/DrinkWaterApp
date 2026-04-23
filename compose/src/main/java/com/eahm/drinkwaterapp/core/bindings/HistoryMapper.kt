package com.eahm.drinkwaterapp.core.bindings

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI

fun List<HistoryEntity>.toUI(): List<HistoryUI> {
    return map { it.toUI() }
}

fun HistoryEntity.toUI(): HistoryUI {
    return HistoryUI(
        id = id,
        amount = amount,
        status = status.ordinal,
        timestamp = timestamp,
    )
}

fun HistoryUI.toEntity(): HistoryEntity {
    return HistoryEntity(
        id = id,
        amount = amount,
        status = runCatching { Status.entries[status] }
            .getOrElse { throw Exception("Failed to parse the status with ordinal: $status") },
        timestamp = timestamp,
    )
}
