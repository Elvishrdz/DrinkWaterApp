package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.InitialConnectionSyncUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser
import com.eahm.drinkwaterapp.backup.domain.model.RemoteBackupStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncDisposition
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class BackupRepositoryImpl(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val statusHolder: BackupSyncStatusHolder,
    private val connectUseCase: ConnectBackupUseCase,
    private val syncUseCase: SyncBackupUseCase,
    private val initialConnectionSyncUseCase: InitialConnectionSyncUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val checkStatusUseCase: CheckBackupStatusUseCase,
    private val disconnectUseCase: DisconnectBackupUseCase,
    private val migrateUseCase: MigrateBackupUseCase,
) : BackupRepository {

    override val syncState: StateFlow<BackupSyncState> = statusHolder.legacyState
    override val syncStatus: StateFlow<BackupSyncStatus> = statusHolder.status

    private suspend fun resolveProvider(): BackupResult<CloudBackupProvider> {
        val providerId = prefs.getBackupProvider()
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        val type = CloudProviderType.fromId(providerId)
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        return BackupResult.Success(registry.get(type))
    }

    override suspend fun connect(
        providerType: CloudProviderType,
    ): BackupResult<CloudUser> = withContext(Dispatchers.IO) {
        statusHolder.onStarted(SyncTrigger.CONNECT)
        connectUseCase(providerType).also { result ->
            when (result) {
                is BackupResult.Success ->
                    statusHolder.onSuccess(SyncTrigger.CONNECT, System.currentTimeMillis())

                is BackupResult.Failure ->
                    statusHolder.onFailure(
                        SyncTrigger.CONNECT,
                        result.error,
                        dispositionFor(result.error),
                    )
            }
        }
    }

    override suspend fun sync(
        allowInitialDecision: Boolean,
        trigger: SyncTrigger,
    ): BackupResult<SyncResult> = withContext(Dispatchers.IO) {
        // Cross-path guard: if any sync (incl. the background worker) is already running, skip.
        // The BackupSyncLock inside the use cases is the real data-safety backstop.
        if (statusHolder.isRunning) {
            return@withContext BackupResult.Success(SyncResult.AlreadyInProgress)
        }

        val provider = resolveProvider()
        if (provider is BackupResult.Failure) {
            statusHolder.onStarted(trigger)
            statusHolder.onFailure(trigger, provider.error, dispositionFor(provider.error))
            return@withContext provider
        }
        val cloudProvider = (provider as BackupResult.Success).value

        statusHolder.onStarted(trigger)

        // First connection still undecided: interactive "Sync now" re-prompts the choice.
        // Otherwise normal sync runs — and SyncBackupUseCase itself skips while a decision
        // is still pending, so automatic/background syncs never merge prematurely.
        val syncCall = if (allowInitialDecision && prefs.getInitialSyncPending()) {
            initialConnectionSyncUseCase(cloudProvider)
        } else {
            syncUseCase(cloudProvider)
        }
        syncCall.also { result ->
            when (result) {
                is BackupResult.Success ->
                    if (result.value is SyncResult.AlreadyInProgress) {
                        // Lost the lock race — leave the prior outcome untouched, don't fake success.
                        statusHolder.onSkipped()
                    } else {
                        statusHolder.onSuccess(trigger, System.currentTimeMillis())
                    }

                is BackupResult.Failure ->
                    statusHolder.onFailure(trigger, result.error, dispositionFor(result.error))
            }
        }
    }

    override suspend fun restore(): BackupResult<Unit> =
        withContext(Dispatchers.IO) {
            val provider = resolveProvider()
            if (provider is BackupResult.Failure) return@withContext provider
            restoreUseCase((provider as BackupResult.Success).value)
        }

    override suspend fun checkRemoteStatus(): BackupResult<RemoteBackupStatus?> =
        withContext(Dispatchers.IO) {
            val provider = resolveProvider()
            if (provider is BackupResult.Failure) return@withContext provider
            checkStatusUseCase((provider as BackupResult.Success).value)
        }

    override suspend fun disconnect(): BackupResult<Unit> =
        withContext(Dispatchers.IO) { disconnectUseCase() }

    override suspend fun migrateProvider(
        from: CloudProviderType,
        to: CloudProviderType,
    ): BackupResult<Unit> = withContext(Dispatchers.IO) {
        val fromProvider = registry.get(from)
        val toProvider = registry.get(to)
        migrateUseCase(fromProvider, toProvider)
    }

    /**
     * Disposition for failures on interactive/connect paths. These are not auto-retried by
     * WorkManager, but the on-save/reconnect machinery re-attempts once connectivity returns —
     * so a network failure is "waiting for connection", everything else needs a manual retry.
     */
    private fun dispositionFor(error: BackupError): SyncDisposition =
        if (error is BackupError.NetworkUnavailable) {
            SyncDisposition.WAITING_FOR_CONNECTION
        } else {
            SyncDisposition.FAILED
        }
}
