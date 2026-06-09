package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.TombstonePolicy

/**
 * Removes tombstone records whose deleted_at is older than [TombstonePolicy.retentionMs].
 * Also sets KEY_TOMBSTONE_WARNING_PENDING if any tombstone is within 3 days of expiry
 * but not yet synced (KEY_SYNC_PENDING == true).
 *
 * NOTE: The actual physical deletion from Room is delegated to [BackupDataSource.purgeTombstones].
 * This use case orchestrates the decision logic.
 */
class PurgeTombstonesUseCase(
    private val dataSource: BackupDataSource,
    private val prefs: BackupPreferences,
    private val policy: TombstonePolicy = TombstonePolicy(),
) {
    companion object {
        private const val WARNING_THRESHOLD_MS = 3L * 24 * 60 * 60 * 1000 // 3 days
    }

    suspend operator fun invoke(): TombstoneCheckResult {
        val now = System.currentTimeMillis()
        val expiryThreshold = now - policy.retentionMs
        val warningThreshold = now - (policy.retentionMs - WARNING_THRESHOLD_MS)

        val nearExpiry = dataSource.findNearExpiryTombstones(warningThreshold, expiryThreshold)
        val syncPending = prefs.getSyncPending()

        // Rule 1: only purge tombstones we're certain reached the cloud.
        //  - Skip entirely while there are unsynced local changes (syncPending).
        //  - Cap the purge cutoff at lastRemoteSyncAt, so a tombstone is removed only if a
        //    successful upload happened AFTER its deletion (deleted_at <= lastRemoteSyncAt).
        // This prevents a long-offline device from purging a deletion before it's uploaded.
        if (!syncPending) {
            val lastRemoteSyncAt = prefs.getLastRemoteSyncAt() ?: 0L
            val safeThreshold = minOf(expiryThreshold, lastRemoteSyncAt)
            dataSource.purgeExpiredTombstones(safeThreshold)
        }

        val shouldWarn = nearExpiry > 0 && syncPending
        prefs.setTombstoneWarningPending(shouldWarn)

        return TombstoneCheckResult(nearExpiryCount = nearExpiry, warningSet = shouldWarn)
    }
}

data class TombstoneCheckResult(
    val nearExpiryCount: Int,
    val warningSet: Boolean,
)
