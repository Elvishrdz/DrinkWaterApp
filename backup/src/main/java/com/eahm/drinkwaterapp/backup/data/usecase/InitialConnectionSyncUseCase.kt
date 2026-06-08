package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.InitialSyncResolver
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.ExistingRemoteBackup
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult

/**
 * Runs the sync that happens right after connecting to a provider.
 *
 * - If the provider has **no** remote backup yet, falls back to the normal automatic
 *   [SyncBackupUseCase] (upload local data).
 * - If a remote backup **already exists**, suspends and asks the user (via
 *   [InitialSyncResolver]) to choose the direction, since the first reconciliation is
 *   destructive and should not silently merge:
 *     - [InitialSyncDecision.USE_REMOTE] → restore remote over local, then settle sync state.
 *     - [InitialSyncDecision.UPLOAD_LOCAL] → overwrite remote with local.
 *     - dismissed (null) → do nothing; the user stays connected and can sync later.
 *
 * Keeps [ConnectBackupUseCase] free of this branching so the concern stays decoupled.
 */
class InitialConnectionSyncUseCase(
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val resolver: InitialSyncResolver,
) {
    suspend operator fun invoke(provider: CloudBackupProvider): BackupResult<SyncResult> {
        val remoteMetadata = when (val status = provider.getRemoteMetadata()) {
            is BackupResult.Success -> status.value
            // Couldn't determine remote state (e.g. network) → defer to normal sync,
            // which has its own error handling.
            is BackupResult.Failure -> return syncUseCase(provider)
        }

        // No remote file → keep the existing automatic behavior.
        if (remoteMetadata == null) return syncUseCase(provider)

        // Remote file exists → the user decides what to do.
        return when (resolver.awaitDecision(ExistingRemoteBackup(remoteMetadata))) {
            InitialSyncDecision.USE_REMOTE -> when (val restore = restoreUseCase(provider)) {
                is BackupResult.Failure -> BackupResult.Failure(restore.error)
                // Local now matches remote; run a normal sync to settle hashes/timestamps.
                is BackupResult.Success -> syncUseCase(provider)
            }

            InitialSyncDecision.UPLOAD_LOCAL -> syncUseCase.overwriteRemoteWithLocal(provider)

            // Dismissed — leave both sides untouched for now.
            null -> BackupResult.Success(SyncResult.SkippedNoChanges)
        }
    }
}
