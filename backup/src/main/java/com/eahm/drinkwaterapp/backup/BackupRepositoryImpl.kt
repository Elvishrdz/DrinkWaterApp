package com.eahm.drinkwaterapp.backup

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BackupRepositoryImpl(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val connectUseCase: ConnectBackupUseCase,
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val checkStatusUseCase: CheckBackupStatusUseCase,
    private val disconnectUseCase: DisconnectBackupUseCase,
    private val migrateUseCase: MigrateBackupUseCase,
) : BackupRepository {

    private val _syncState = MutableStateFlow<BackupSyncState>(BackupSyncState.Idle)
    override val syncState: StateFlow<BackupSyncState> = _syncState.asStateFlow()

    private suspend fun resolveProvider(): BackupResult<CloudBackupProvider> {
        val providerId = prefs.getBackupProvider()
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        val type = CloudProviderType.fromId(providerId)
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        return BackupResult.Success(registry.get(type))
    }

    override suspend fun connect(
        context: Context?,
        providerType: CloudProviderType,
    ): BackupResult<CloudUser> {
        _syncState.value = BackupSyncState.InProgress
        return connectUseCase(context, providerType).also { result ->
            _syncState.value = when (result) {
                is BackupResult.Success -> BackupSyncState.Success(System.currentTimeMillis())
                is BackupResult.Failure -> BackupSyncState.Error(result.error)
            }
        }
    }

    override suspend fun sync(context: Context?): BackupResult<SyncResult> {
        if (_syncState.value is BackupSyncState.InProgress) {
            return BackupResult.Success(SyncResult.AlreadyInProgress)
        }
        _syncState.value = BackupSyncState.InProgress
        val provider = resolveProvider()
        if (provider is BackupResult.Failure) {
            _syncState.value = BackupSyncState.Error(provider.error)
            return provider
        }
        return syncUseCase(context, (provider as BackupResult.Success).value).also { result ->
            _syncState.value = when (result) {
                is BackupResult.Success -> BackupSyncState.Success(System.currentTimeMillis())
                is BackupResult.Failure -> BackupSyncState.Error(result.error)
            }
        }
    }

    override suspend fun restore(context: Context?): BackupResult<Unit> {
        val provider = resolveProvider()
        if (provider is BackupResult.Failure) return provider
        return restoreUseCase(context, (provider as BackupResult.Success).value)
    }

    override suspend fun checkRemoteStatus(context: Context?): BackupResult<RemoteBackupStatus?> {
        val provider = resolveProvider()
        if (provider is BackupResult.Failure) return provider
        return checkStatusUseCase(context, (provider as BackupResult.Success).value)
    }

    override suspend fun disconnect(context: Context?): BackupResult<Unit> =
        disconnectUseCase(context)

    override suspend fun migrateProvider(
        context: Context?,
        from: CloudProviderType,
        to: CloudProviderType,
    ): BackupResult<Unit> {
        val fromProvider = registry.get(from)
        val toProvider = registry.get(to)
        return migrateUseCase(context, fromProvider, toProvider)
    }
}
