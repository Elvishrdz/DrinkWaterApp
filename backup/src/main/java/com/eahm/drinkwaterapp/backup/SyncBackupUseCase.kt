package com.eahm.drinkwaterapp.backup

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import java.security.MessageDigest

class SyncBackupUseCase(
    private val mutex: Mutex,
    private val dataSource: BackupDataSource,
    private val prefs: BackupPreferences,
    private val mergeEngine: MergeEngine,
    private val conflictResolver: SyncConflictResolver,
) {

    suspend operator fun invoke(
        context: Context?,
        provider: CloudBackupProvider,
    ): BackupResult<SyncResult> {
        if (!mutex.tryLock()) return BackupResult.Success(SyncResult.AlreadyInProgress)

        return try {
            val deviceId = prefs.getDeviceId()

            // Step 1: Try to download the remote file
            val downloadResult = provider.download(context)

            // First sync: no remote file yet — just upload local data
            if (downloadResult is BackupResult.Failure &&
                downloadResult.error is BackupError.FileNotFound
            ) {
                return uploadLocal(context, provider, deviceId)
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

            // Step 3: Parse remote into BackupData
            val remoteData = dataSource.parseRemoteData(migratedRaw)
                ?: return BackupResult.Failure(BackupError.MergeCorrupted)

            // Step 4: Read local data
            val localData = dataSource.readLocalData(deviceId)

            // Step 5: Merge
            val mergeResult = mergeEngine.merge(localData, remoteData, deviceId)

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
            val uploadResult = provider.upload(context, serialized)
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

            BackupResult.Success(SyncResult.Uploaded)

        } finally {
            mutex.unlock()
        }
    }

    private suspend fun uploadLocal(
        context: Context?,
        provider: CloudBackupProvider,
        deviceId: String,
    ): BackupResult<SyncResult> {
        val serialized = dataSource.serialize()
        val newHash = sha256(serialized)
        val storedHash = prefs.getBackupContentHash()

        if (newHash == storedHash) {
            return BackupResult.Success(SyncResult.SkippedNoChanges)
        }

        val uploadResult = provider.upload(context, serialized)
        return if (uploadResult is BackupResult.Success) {
            val now = System.currentTimeMillis()
            prefs.setBackupContentHash(newHash)
            prefs.setBackupLastSyncAt(now)
            prefs.setLastRemoteSyncAt(now)
            prefs.setBackupLastError(null)
            prefs.setSyncPending(false)
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
