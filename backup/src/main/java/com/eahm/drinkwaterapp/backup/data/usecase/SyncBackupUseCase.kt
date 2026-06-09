package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CorruptedBackup
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.domain.model.TombstonePolicy
import kotlinx.coroutines.sync.Mutex
import java.security.MessageDigest

class SyncBackupUseCase(
    private val mutex: Mutex,
    private val dataSource: BackupDataSource,
    private val prefs: BackupPreferences,
    private val mergeEngine: MergeEngine,
    private val conflictResolver: SyncConflictResolver,
    private val recoveryResolver: BackupRecoveryResolver,
    private val purgeTombstonesUseCase: PurgeTombstonesUseCase,
    private val tombstonePolicy: TombstonePolicy,
) {

    suspend operator fun invoke(
        provider: CloudBackupProvider,
    ): BackupResult<SyncResult> {
        // Never merge automatically while the user still owes a first-connection decision.
        // The interactive flow (InitialConnectionSyncUseCase) clears this before syncing.
        if (prefs.getInitialSyncPending()) {
            return BackupResult.Success(SyncResult.SkippedNoChanges)
        }
        if (!mutex.tryLock()) return BackupResult.Success(SyncResult.AlreadyInProgress)

        return try {
            val deviceId = prefs.getDeviceId()

            // Step 1: Try to download the remote file
            val downloadResult = provider.download()

            // First sync: no remote file yet — just upload local data
            if (downloadResult is BackupResult.Failure &&
                downloadResult.error is BackupError.FileNotFound
            ) {
                return uploadLocal(provider, deviceId)
            }

            // Remote file exists but doesn't match our spec → ask the user what to do.
            if (downloadResult is BackupResult.Failure &&
                downloadResult.error is BackupError.RemoteCorrupted
            ) {
                return handleCorruptedRemote(
                    provider = provider,
                    deviceId = deviceId,
                    reason = CorruptedBackup.Reason.UNREADABLE,
                )
            }

            // Any other download error → propagate
            if (downloadResult is BackupResult.Failure) {
                prefs.setBackupLastError(downloadResult.error.javaClass.simpleName)
                return downloadResult
            }

            val remoteFile = (downloadResult as BackupResult.Success).value

            // Step 2: Schema compatibility check + migration
            val remoteSchema = remoteFile.metadata.schemaVersion
            if (remoteSchema > BackupMigrationManager.currentSchema) {
                return BackupResult.Failure(BackupError.IncompatibleVersion(remoteSchema))
            }

            val migratedRaw = if (remoteSchema < BackupMigrationManager.currentSchema) {
                when (val m = BackupMigrationManager.migrate(remoteFile.rawData, remoteSchema)) {
                    is BackupResult.Success -> m.value
                    is BackupResult.Failure -> return m
                }
            } else {
                remoteFile.rawData
            }

            // Step 3: Parse remote into BackupData. A null here means the file was
            // readable but its structure is unrecognizable → user recovery decision.
            val remoteData = dataSource.parseRemoteData(migratedRaw)
                ?: return handleCorruptedRemote(
                    provider = provider,
                    deviceId = deviceId,
                    reason = CorruptedBackup.Reason.UNRECOGNIZED_STRUCTURE,
                )

            // Step 4: Read local data
            val localData = dataSource.readLocalData(deviceId)

            // Step 5: Merge (retention-aware so expired tombstones aren't resurrected)
            val expiryThreshold = System.currentTimeMillis() - tombstonePolicy.retentionMs
            val mergeResult = mergeEngine.merge(localData, remoteData, deviceId, expiryThreshold)

            // Step 6: Resolve user conflicts (suspends per conflict)
            var mergedData = mergeResult.mergedData
            for (conflict in mergeResult.conflicts) {
                val useRemote = conflictResolver.awaitResolution(conflict)
                mergedData = when (conflict) {
                    is SyncConflict.GoalConflict ->
                        mergeEngine.resolveGoalConflict(mergedData, conflict, useRemote)

                    is SyncConflict.PreferencesConflict ->
                        mergeEngine.resolvePrefsConflict(mergedData, conflict, useRemote)
                }
            }

            // Step 7: Write merged data back to local DB
            val writeResult = dataSource.writeBackupData(mergedData)
            if (writeResult is BackupResult.Failure) return writeResult

            // Step 8: Update seen devices registry with the remote device
            updateSeenDevices(remoteFile.metadata.deviceId, "")

            // Step 9: Serialize merged result and check if upload is needed
            val serialized = dataSource.serialize()
            val newHash = sha256(serialized)
            val storedHash = prefs.getBackupContentHash()

            if (newHash == storedHash && mergeResult.appliedRemoteRecords == 0) {
                return BackupResult.Success(SyncResult.SkippedNoChanges)
            }

            // Step 10: Upload
            val uploadResult = provider.upload(serialized)
            if (uploadResult is BackupResult.Failure) {
                prefs.setBackupLastError(uploadResult.error.javaClass.simpleName)
                return uploadResult
            }

            val now = System.currentTimeMillis()
            prefs.setBackupContentHash(newHash)
            prefs.setBackupLastSyncAt(now)
            prefs.setLastRemoteSyncAt(now)
            prefs.setBackupLastError(null)
            prefs.setSyncPending(false)

            // Purge AFTER a successful upload: our tombstones are now in the cloud, so
            // physically removing expired+synced ones can't lose a deletion (Rules 1 & 2).
            purgeTombstonesUseCase()

            BackupResult.Success(SyncResult.Uploaded)

        } finally {
            mutex.unlock()
        }
    }

    /**
     * Remote backup is unusable. Ask the user (suspends) whether to overwrite it with
     * this device's data. If they decline, the remote file is left exactly as-is.
     * Overwriting is irreversible — the previous remote contents cannot be recovered.
     */
    /**
     * Force-uploads local data, replacing the remote backup regardless of its contents.
     * Used by the first-connection flow when the user chooses to keep this device's data.
     */
    suspend fun overwriteRemoteWithLocal(
        provider: CloudBackupProvider,
    ): BackupResult<SyncResult> {
        if (!mutex.tryLock()) return BackupResult.Success(SyncResult.AlreadyInProgress)
        return try {
            uploadLocal(provider, prefs.getDeviceId(), force = true)
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun handleCorruptedRemote(
        provider: CloudBackupProvider,
        deviceId: String,
        reason: CorruptedBackup.Reason,
    ): BackupResult<SyncResult> {
        prefs.setBackupLastError(BackupError.RemoteCorrupted.javaClass.simpleName)
        val overwrite = recoveryResolver.awaitDecision(CorruptedBackup(reason))
        return if (overwrite) {
            uploadLocal(provider, deviceId, force = true)
        } else {
            // User declined — do nothing to the backup.
            BackupResult.Success(SyncResult.SkippedNoChanges)
        }
    }

    private suspend fun uploadLocal(
        provider: CloudBackupProvider,
        deviceId: String,
        force: Boolean = false,
    ): BackupResult<SyncResult> {
        val serialized = dataSource.serialize()
        val newHash = sha256(serialized)
        val storedHash = prefs.getBackupContentHash()

        if (!force && newHash == storedHash) {
            return BackupResult.Success(SyncResult.SkippedNoChanges)
        }

        val uploadResult = provider.upload(serialized)

        return if (uploadResult is BackupResult.Success) {
            val now = System.currentTimeMillis()
            prefs.setBackupContentHash(newHash)
            prefs.setBackupLastSyncAt(now)
            prefs.setLastRemoteSyncAt(now)
            prefs.setBackupLastError(null)
            prefs.setSyncPending(false)
            // Purge expired+synced tombstones now that the upload (which included them) landed.
            purgeTombstonesUseCase()
            BackupResult.Success(SyncResult.Uploaded)
        } else {
            val failure = uploadResult as BackupResult.Failure
            prefs.setBackupLastError(failure.error.javaClass.simpleName)
            failure
        }
    }

    private suspend fun updateSeenDevices(remoteDeviceId: String, remoteDeviceModel: String) {
        if (remoteDeviceId.isBlank()) return
        val existing = prefs.getSeenDevices().toMutableList()
        val idx = existing.indexOfFirst { it.deviceId == remoteDeviceId }
        val updated = SeenDevice(
            deviceId = remoteDeviceId,
            deviceModel = remoteDeviceModel,
            lastSeenAt = System.currentTimeMillis(),
            isCurrentDevice = false,
        )
        if (idx >= 0) existing[idx] = updated else existing.add(updated)
        prefs.setSeenDevices(existing)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
