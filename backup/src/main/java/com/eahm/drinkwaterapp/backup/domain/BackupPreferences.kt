package com.eahm.drinkwaterapp.backup.domain

import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice

interface BackupPreferences {
    suspend fun getBackupEnabled(): Boolean
    suspend fun setBackupEnabled(enabled: Boolean)

    suspend fun getBackupProvider(): String?
    suspend fun setBackupProvider(id: String)

    suspend fun getBackupLastSyncAt(): Long?
    suspend fun setBackupLastSyncAt(millis: Long)

    /** Hour of day (0–23) at which the daily auto-backup should run. */
    suspend fun getBackupAutoHour(): Int
    suspend fun setBackupAutoHour(hour: Int)

    /** Which scheduling mode is active: "DAILY" (fixed hour) or "INTERVAL" (every N hours). */
    suspend fun getBackupScheduleMode(): String?
    suspend fun setBackupScheduleMode(mode: String)

    /** Interval in hours (e.g. 2, 4, 6) used when the schedule mode is INTERVAL. */
    suspend fun getBackupIntervalHours(): Int
    suspend fun setBackupIntervalHours(hours: Int)

    suspend fun getBackupUserDisplayName(): String?
    suspend fun setBackupUserDisplayName(name: String?)

    suspend fun getBackupContentHash(): String?
    suspend fun setBackupContentHash(hash: String?)

    suspend fun getBackupLastError(): String?
    suspend fun setBackupLastError(code: String?)

    /**
     * Persisted last-failure descriptor in the form "TRIGGER|DISPOSITION" so the Settings UI can
     * restore the failure message after process death. Null when the last sync succeeded.
     */
    suspend fun getBackupLastFailure(): String?
    suspend fun setBackupLastFailure(value: String?)

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

    /**
     * True when the provider has a remote backup that the user has not yet decided how to
     * reconcile on first connection. While true, interactive "Sync now" re-prompts the
     * initial-sync choice and automatic syncs are skipped.
     */
    suspend fun getInitialSyncPending(): Boolean
    suspend fun setInitialSyncPending(pending: Boolean)
}
