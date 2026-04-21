package com.eahm.drinkwaterapp.core.database.converter

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class InstantConverter {

    @TypeConverter
    fun fromEpochMillis(
        value: Long?,
    ): Instant? {
        return value?.let {
            Instant.fromEpochMilliseconds(it)
        }
    }

    @TypeConverter
    fun toEpochMillis(
        instant: Instant?,
    ): Long? {
        return instant?.toEpochMilliseconds()
    }
}
