package com.eahm.drinkwaterapp.backup.domain.model

/** How the automatic background backup recurs. */
sealed interface BackupSchedule {
    /** Once per day at a fixed clock hour (0–23). */
    data class Daily(val hourOfDay: Int) : BackupSchedule

    /** Every [hours] hours (e.g. 2, 4, 6). */
    data class EveryHours(val hours: Int) : BackupSchedule
}

/** Persisted selector for which kind of [BackupSchedule] the user picked. */
enum class BackupScheduleMode {
    DAILY,
    INTERVAL;

    companion object {
        fun fromName(name: String?): BackupScheduleMode =
            entries.firstOrNull { it.name == name } ?: DAILY
    }
}
