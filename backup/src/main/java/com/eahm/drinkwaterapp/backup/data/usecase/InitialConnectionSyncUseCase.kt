package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.data.InitialSyncResolver
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.ExistingRemoteBackup
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult

/**
 * Runs the sync that happens right after connecting to a provider.
 *
 * - No remote backup yet → normal automatic [SyncBackupUseCase] (upload local).
 * - Remote backup exists but is **byte-for-byte identical** to local content → silently
 *   keep it; no user prompt (the optimized [BackupDataSource.localContentFingerprint] /
 *   [BackupDataSource.remoteContentFingerprint] comparison proves exact equality without a
 *   record-by-record diff).
 * - Remote backup exists and **differs** → suspend and ask the user (via
 *   [InitialSyncResolver]) for the destructive direction:
 *     - [InitialSyncDecision.USE_REMOTE] → restore remote over local, then settle state.
 *     - [InitialSyncDecision.UPLOAD_LOCAL] → overwrite remote with local.
 *     - dismissed (null) → leave both untouched; flag stays pending so "Sync now" re-asks.
 *
 * Keeps [ConnectBackupUseCase] free of this branching so the concern stays decoupled.
 */
class InitialConnectionSyncUseCase(
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val resolver: InitialSyncResolver,
    private val prefs: BackupPreferences,
    private val dataSource: BackupDataSource,
) {
    suspend operator fun invoke(provider: CloudBackupProvider): BackupResult<SyncResult> {
        val downloadResult = provider.download()
        val backupFile = when (downloadResult) {
            is BackupResult.Success -> downloadResult.value
            is BackupResult.Failure -> {
                // No remote file → nothing to decide; keep the automatic behavior.
                if (downloadResult.error is BackupError.FileNotFound) {
                    prefs.setInitialSyncPending(false)
                    return syncUseCase(provider)
                }
                // Corrupted / network / other → defer to the normal sync, which has its
                // own recovery and error handling. Leave the pending flag untouched.
                return syncUseCase(provider)
            }
        }

        // A remote newer than this app can't be compared or merged → defer to normal sync,
        // which surfaces IncompatibleVersion.
        val remoteSchema = backupFile.metadata.schemaVersion
        if (remoteSchema > BackupMigrationManager.currentSchema) {
            return syncUseCase(provider)
        }

        val migratedRaw = if (remoteSchema < BackupMigrationManager.currentSchema) {
            when (val m = BackupMigrationManager.migrate(backupFile.rawData, remoteSchema)) {
                is BackupResult.Success -> m.value
                is BackupResult.Failure -> return syncUseCase(provider)
            }
        } else {
            backupFile.rawData
        }

        // Optimized exact-equality check: one hash per side, compared. If the remote holds
        // exactly the same logs and configuration as local, just keep it — no user prompt.
        val remoteFingerprint = dataSource.remoteContentFingerprint(migratedRaw)
        val localFingerprint = dataSource.localContentFingerprint()
        if (remoteFingerprint == localFingerprint) {
            prefs.setInitialSyncPending(false)
            return syncUseCase(provider)
        }

        // Content differs → the user decides the direction.
        prefs.setInitialSyncPending(true)

        return when (resolver.awaitDecision(ExistingRemoteBackup(backupFile.metadata))) {
            InitialSyncDecision.USE_REMOTE -> when (val restore = restoreUseCase(provider)) {
                is BackupResult.Failure -> BackupResult.Failure(restore.error)
                is BackupResult.Success -> {
                    prefs.setInitialSyncPending(false)
                    syncUseCase(provider)
                }
            }

            InitialSyncDecision.UPLOAD_LOCAL -> syncUseCase.overwriteRemoteWithLocal(provider)
                .also { if (it is BackupResult.Success) prefs.setInitialSyncPending(false) }

            null -> BackupResult.Success(SyncResult.SkippedNoChanges)
        }
    }
}
