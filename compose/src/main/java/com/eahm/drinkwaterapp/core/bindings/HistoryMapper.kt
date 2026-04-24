package com.eahm.drinkwaterapp.core.bindings

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI

fun HistoryEntity.toUI(): HistoryUI {
    return HistoryUI(
        id = id,
        amount = if (amount % 1 == 0f) {
            amount.toInt().toString()
        } else {
            amount.toString()
        },
        status = status.ordinal,
        timestamp = timestamp,
    )
}

fun HistoryUI.toEntity(): HistoryEntity {
    return HistoryEntity(
        id = id,
        amount = amount.toFloat(),
        status = runCatching { Status.entries[status] }
            .getOrElse { throw Exception("Failed to parse the status with ordinal: $status") },
        timestamp = timestamp,
    )
}
