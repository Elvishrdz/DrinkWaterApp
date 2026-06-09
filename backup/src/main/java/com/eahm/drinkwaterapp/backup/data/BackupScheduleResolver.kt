package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.BackupSchedule
import com.eahm.drinkwaterapp.backup.domain.model.BackupScheduleMode

/**
 * Builds the active [BackupSchedule] from persisted preferences. Shared by the app-start
 * initializer, the Settings screen, and the worker's self-reschedule so they all agree.
 */
suspend fun BackupPreferences.currentBackupSchedule(): BackupSchedule =
    when (BackupScheduleMode.fromName(getBackupScheduleMode())) {
        BackupScheduleMode.INTERVAL -> BackupSchedule.EveryHours(getBackupIntervalHours())
        BackupScheduleMode.DAILY -> BackupSchedule.Daily(getBackupAutoHour())
    }
