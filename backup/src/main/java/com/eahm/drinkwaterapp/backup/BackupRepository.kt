package com.eahm.drinkwaterapp.backup

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface BackupRepository {
    val syncState: StateFlow<BackupSyncState>

    suspend fun connect(context: Context, providerType: CloudProviderType): BackupResult<CloudUser>
    suspend fun sync(context: Context): BackupResult<SyncResult>
    suspend fun restore(context: Context): BackupResult<Unit>
    suspend fun checkRemoteStatus(context: Context): BackupResult<RemoteBackupStatus?>
    suspend fun disconnect(context: Context): BackupResult<Unit>
    suspend fun migrateProvider(
        context: Context,
        from: CloudProviderType,
        to: CloudProviderType,
    ): BackupResult<Unit>
}
