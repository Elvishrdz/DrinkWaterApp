package com.eahm.drinkwaterapp.backup

interface BackupScheduler {
    fun scheduleAutoSync(intervalDays: Int = 1)
    fun triggerManualSync()
    fun cancelAll()
}
