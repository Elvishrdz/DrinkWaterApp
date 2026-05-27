package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.BackupScheduler

class FakeBackupScheduler : BackupScheduler {
    var scheduleAutoSyncCalled = false
    var lastScheduledIntervalDays: Int = -1
    var triggerManualSyncCalled = false
    var cancelAllCalled = false

    override fun scheduleAutoSync(intervalDays: Int) {
        scheduleAutoSyncCalled = true
        lastScheduledIntervalDays = intervalDays
    }

    override fun triggerManualSync() {
        triggerManualSyncCalled = true
    }

    override fun cancelAll() {
        cancelAllCalled = true
    }
}
