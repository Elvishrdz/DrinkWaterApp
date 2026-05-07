package com.eahm.drinkwaterapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "daily_goal")
@OptIn(ExperimentalUuidApi::class)
data class DailyGoalEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    @ColumnInfo(name = "effective_date") val effectiveDate: LocalDate,
    @ColumnInfo(name = "goal_ml") val goalMl: Float,
)
