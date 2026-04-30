package com.eahm.drinkwaterapp.core.domain.mock

import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
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

val Mock.hourGroupMock
    get() = listOf(
        HourGroup(
            hour = 8,
            isCurrentHour = false,
            items = Mock.recordsMock.map { it.toUI() }
        ),
        HourGroup(
            hour = 9,
            isCurrentHour = false,
            items = Mock.recordsMock.map { it.toUI() }
        ),
    )

val Mock.drinkStatsMock
    get() = DrinkStats(
        goalAmount = 1000f,
        drinkAmount = 200f,
        goalReached = false,
    )
