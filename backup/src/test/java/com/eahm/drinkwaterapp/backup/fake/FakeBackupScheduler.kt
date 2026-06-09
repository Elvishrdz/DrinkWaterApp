package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.BackupSchedule

class FakeBackupScheduler : BackupScheduler {
    var scheduleAutoSyncCalled = false
    var lastSchedule: BackupSchedule? = null
    var triggerManualSyncCalled = false
    var triggerOnChangeSyncCalled = false
    var enqueueFollowUpOnChangeSyncCount = 0
    var triggerForegroundSyncCalled = false
    var cancelAllCalled = false

    override fun scheduleAutoSync(schedule: BackupSchedule) {
        scheduleAutoSyncCalled = true
        lastSchedule = schedule
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
