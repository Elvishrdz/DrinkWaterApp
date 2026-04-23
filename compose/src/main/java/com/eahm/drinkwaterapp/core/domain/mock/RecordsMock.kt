package com.eahm.drinkwaterapp.core.domain.mock

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.datetime.LocalDateTime

object Mock

val Mock.localDateTimeMock
    get() = LocalDateTime(
        year = 2023,
        month = 1,
        day = 1,
        hour = 10,
        minute = 5,
    )

val Mock.recordsMock
    get() = listOf(
        HistoryEntity(
            amount = 200f,
            status = Status.IN_APP,
            timestamp = localDateTimeMock,
        ),
        HistoryEntity(
            amount = 200f,
            status = Status.IN_APP,
            timestamp = localDateTimeMock,
        )
    )
