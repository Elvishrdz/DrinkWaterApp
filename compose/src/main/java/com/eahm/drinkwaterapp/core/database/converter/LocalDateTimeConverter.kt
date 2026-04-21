package com.eahm.drinkwaterapp.core.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class LocalDateTimeConverter {


    @TypeConverter
    fun fromEpochMillis(
        value: Long?,
    ): LocalDateTime? {
        return value?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
        }
    }

    @TypeConverter
    fun localDateTimeToEpochMillis(
        dateTime: LocalDateTime?,
    ): Long? {
        return dateTime?.toInstant(TimeZone.UTC)
            ?.toEpochMilliseconds()
    }
}
