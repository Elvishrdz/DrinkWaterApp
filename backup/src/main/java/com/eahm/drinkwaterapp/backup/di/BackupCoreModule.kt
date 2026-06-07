package com.eahm.drinkwaterapp.backup.di

import com.eahm.drinkwaterapp.backup.BackupRepository
import com.eahm.drinkwaterapp.backup.BackupRepositoryImpl
import com.eahm.drinkwaterapp.backup.ConnectivityObserver
import com.eahm.drinkwaterapp.backup.SyncCoordinator
import com.eahm.drinkwaterapp.backup.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.MergeEngine
import com.eahm.drinkwaterapp.backup.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.PurgeTombstonesUseCase
import com.eahm.drinkwaterapp.backup.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.TombstonePolicy
import kotlinx.coroutines.sync.Mutex
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val backupCoreModule = module {
    single { Mutex() }

    // BackupProviderRegistry is registered by the host module (BackupModule in :compose)
    // using explicit typed get() because both params are CloudBackupProvider.

    singleOf(::ConnectBackupUseCase)
    singleOf(::SyncBackupUseCase)
    singleOf(::RestoreBackupUseCase)
    singleOf(::CheckBackupStatusUseCase)
    singleOf(::DisconnectBackupUseCase)
    singleOf(::MigrateBackupUseCase)

    singleOf(::BackupRepositoryImpl) bind BackupRepository::class

    singleOf(::SyncConflictResolver)
    singleOf(::MergeEngine)
    single { TombstonePolicy() }
    singleOf(::PurgeTombstonesUseCase)

    singleOf(::SyncCoordinator)
    single { ConnectivityObserver(androidContext()) }
}
