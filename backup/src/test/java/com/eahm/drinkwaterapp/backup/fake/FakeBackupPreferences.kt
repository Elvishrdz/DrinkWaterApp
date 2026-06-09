package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice

class FakeBackupPreferences : BackupPreferences {
    var backupEnabled: Boolean = false
    var backupProvider: String? = null
    var backupLastSyncAt: Long? = null
    var backupAutoHour: Int = 2
    var backupScheduleMode: String? = null
    var backupIntervalHours: Int = 6
    var backupUserDisplayName: String? = null
    var backupContentHash: String? = null
    var backupLastError: String? = null
    var backupLastFailure: String? = null
    var clearCalled: Boolean = false

    override suspend fun getBackupEnabled() = backupEnabled
    override suspend fun setBackupEnabled(enabled: Boolean) {
        backupEnabled = enabled
    }

    override suspend fun getBackupProvider() = backupProvider
    override suspend fun setBackupProvider(id: String) {
        backupProvider = id
    }

    override suspend fun getBackupLastSyncAt() = backupLastSyncAt
    override suspend fun setBackupLastSyncAt(millis: Long) {
        backupLastSyncAt = millis
    }

    override suspend fun getBackupAutoHour() = backupAutoHour
    override suspend fun setBackupAutoHour(hour: Int) {
        backupAutoHour = hour
    }

    override suspend fun getBackupScheduleMode() = backupScheduleMode
    override suspend fun setBackupScheduleMode(mode: String) {
        backupScheduleMode = mode
    }

    override suspend fun getBackupIntervalHours() = backupIntervalHours
    override suspend fun setBackupIntervalHours(hours: Int) {
        backupIntervalHours = hours
    }

    override suspend fun getBackupUserDisplayName() = backupUserDisplayName
    override suspend fun setBackupUserDisplayName(name: String?) {
        backupUserDisplayName = name
    }

    override suspend fun getBackupContentHash() = backupContentHash
    override suspend fun setBackupContentHash(hash: String?) {
        backupContentHash = hash
    }

    override suspend fun getBackupLastError() = backupLastError
    override suspend fun setBackupLastError(code: String?) {
        backupLastError = code
    }

    override suspend fun getBackupLastFailure() = backupLastFailure
    override suspend fun setBackupLastFailure(value: String?) {
        backupLastFailure = value
    }

    override suspend fun clearBackupKeys() {
        clearCalled = true
        backupEnabled = false
        backupProvider = null
        backupLastSyncAt = null
        backupAutoHour = 2
        backupScheduleMode = null
        backupIntervalHours = 6
        backupUserDisplayName = null
        backupContentHash = null
        backupLastError = null
        backupLastFailure = null
        deviceId = ""
        syncPending = false
        lastRemoteSyncAt = null
        syncOnSaveEnabled = true
        seenDevices = emptyList()
        tombstoneWarningPending = false
    }

    var deviceId: String = ""
    var syncPending: Boolean = false
    var lastRemoteSyncAt: Long? = null
    var syncOnSaveEnabled: Boolean = true
    var seenDevices: List<SeenDevice> = emptyList()
    var tombstoneWarningPending: Boolean = false

    override suspend fun getDeviceId() = deviceId
    override suspend fun setDeviceId(id: String) {
        deviceId = id
    }

    override suspend fun getSyncPending() = syncPending
    override suspend fun setSyncPending(pending: Boolean) {
        syncPending = pending
    }

    override suspend fun getLastRemoteSyncAt() = lastRemoteSyncAt
    override suspend fun setLastRemoteSyncAt(millis: Long) {
        lastRemoteSyncAt = millis
    }

    override suspend fun getSyncOnSaveEnabled() = syncOnSaveEnabled
    override suspend fun setSyncOnSaveEnabled(enabled: Boolean) {
        syncOnSaveEnabled = enabled
    }

    override suspend fun getSeenDevices() = seenDevices
    override suspend fun setSeenDevices(devices: List<SeenDevice>) {
        seenDevices = devices
    }

    override suspend fun getTombstoneWarningPending() = tombstoneWarningPending
    override suspend fun setTombstoneWarningPending(pending: Boolean) {
        tombstoneWarningPending = pending
    }

    var initialSyncPending: Boolean = false
    override suspend fun getInitialSyncPending() = initialSyncPending
    override suspend fun setInitialSyncPending(pending: Boolean) {
        initialSyncPending = pending
    }
}
