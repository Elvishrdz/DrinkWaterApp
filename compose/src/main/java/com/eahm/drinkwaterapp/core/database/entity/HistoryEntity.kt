package com.eahm.drinkwaterapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
data class HistoryEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    @ColumnInfo(name = "drink_amount") val amount: Float,
    @ColumnInfo(name = "status") val status: Status,
    @ColumnInfo(name = "timestamp") val timestamp: LocalDateTime =
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()),
)

enum class Status {
    IN_APP,
    FROM_NOTIFICATION,
    SKIPPED_IN_APP_FROM_NOTIFICATION,
    SKIPPED_FROM_NOTIFICATION,
    SKIPPED_NOTIFICATION_CLOSED,
}
