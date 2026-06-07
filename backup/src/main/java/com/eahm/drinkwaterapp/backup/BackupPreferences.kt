package com.eahm.drinkwaterapp.backup

interface BackupPreferences {
    suspend fun getBackupEnabled(): Boolean
    suspend fun setBackupEnabled(enabled: Boolean)

    suspend fun getBackupProvider(): String?
    suspend fun setBackupProvider(id: String)

    suspend fun getBackupLastSyncAt(): Long?
    suspend fun setBackupLastSyncAt(millis: Long)

    suspend fun getBackupAutoIntervalDays(): Int
    suspend fun setBackupAutoIntervalDays(days: Int)

    suspend fun getBackupUserDisplayName(): String?
    suspend fun setBackupUserDisplayName(name: String?)

    suspend fun getBackupContentHash(): String?
    suspend fun setBackupContentHash(hash: String?)

    suspend fun getBackupLastError(): String?
    suspend fun setBackupLastError(code: String?)

    suspend fun clearBackupKeys()

    // --- Sync / device identity ---
    suspend fun getDeviceId(): String
    suspend fun setDeviceId(id: String)

    suspend fun getSyncPending(): Boolean
    suspend fun setSyncPending(pending: Boolean)

    suspend fun getLastRemoteSyncAt(): Long?
    suspend fun setLastRemoteSyncAt(millis: Long)

    suspend fun getSyncOnSaveEnabled(): Boolean
    suspend fun setSyncOnSaveEnabled(enabled: Boolean)

    suspend fun getSeenDevices(): List<SeenDevice>
    suspend fun setSeenDevices(devices: List<SeenDevice>)

    suspend fun getTombstoneWarningPending(): Boolean
    suspend fun setTombstoneWarningPending(pending: Boolean)
}
