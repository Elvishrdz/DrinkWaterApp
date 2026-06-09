package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.domain.BackupScheduler

class FakeBackupScheduler : BackupScheduler {
    var scheduleDailyAutoSyncCalled = false
    var lastScheduledHour: Int = -1
    var triggerManualSyncCalled = false
    var triggerOnChangeSyncCalled = false
    var enqueueFollowUpOnChangeSyncCount = 0
    var triggerForegroundSyncCalled = false
    var cancelAllCalled = false

    override fun scheduleDailyAutoSync(hourOfDay: Int) {
        scheduleDailyAutoSyncCalled = true
        lastScheduledHour = hourOfDay
    }

    override fun triggerManualSync() {
        triggerManualSyncCalled = true
    }

    override fun cancelAll() {
        cancelAllCalled = true
    }

    override fun triggerOnChangeSync() {
        triggerOnChangeSyncCalled = true
    }

    override fun enqueueFollowUpOnChangeSync() {
        enqueueFollowUpOnChangeSyncCount++
    }

    override fun triggerForegroundSync() {
        triggerForegroundSyncCalled = true
    }
}
