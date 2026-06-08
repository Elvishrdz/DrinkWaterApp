package com.eahm.drinkwaterapp.backup.domain.model

/**
 * In-memory snapshot of all syncable app data, used by MergeEngine.
 * These are plain data structures, independent of Room entities.
 */
data class BackupData(
    val history: List<HistoryRecord>,
    val dailyGoals: List<GoalRecord>,
    val preferences: PreferencesRecord,
)

data class HistoryRecord(
    val id: String,
    val amount: Float,
    val status: String,
    val timestamp: Long,        // epoch ms
    val deviceId: String = "",
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

data class GoalRecord(
    val id: String,
    val effectiveDate: Int,     // epoch days
    val goalMl: Float,
    val deviceId: String = "",
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

data class PreferencesRecord(
    val unitPreference: String,
    val timeFormat: String,
    val amountPerDrink: Float,
    val deviceId: String = "",
    val updatedAt: Long = 0L,
)
