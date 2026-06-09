package com.eahm.drinkwaterapp.backup.di

import com.eahm.drinkwaterapp.backup.data.BackupChangeTracker
import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.BackupRepositoryImpl
import com.eahm.drinkwaterapp.backup.data.BackupSyncLock
import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.data.InitialSyncResolver
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.SyncCoordinator
import com.eahm.drinkwaterapp.backup.data.observer.ConnectivityObserver
import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.InitialConnectionSyncUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.PurgeTombstonesUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.model.TombstonePolicy
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val backupCoreModule = module {
    singleOf(::BackupSyncLock)
    singleOf(::BackupSyncStatusHolder)

    // BackupProviderRegistry is registered by the host module (BackupModule in :compose)
    // using explicit typed get() because both params are CloudBackupProvider.

    singleOf(::ConnectBackupUseCase)
    singleOf(::InitialConnectionSyncUseCase)
    singleOf(::SyncBackupUseCase)
    singleOf(::RestoreBackupUseCase)
    singleOf(::CheckBackupStatusUseCase)
    singleOf(::DisconnectBackupUseCase)
    singleOf(::MigrateBackupUseCase)

    singleOf(::BackupRepositoryImpl) bind BackupRepository::class

    singleOf(::SyncConflictResolver)
    singleOf(::BackupRecoveryResolver)
    singleOf(::InitialSyncResolver)
    singleOf(::MergeEngine)
    single { TombstonePolicy() }
    single { PurgeTombstonesUseCase(get(), get(), get()) }
    single { BackupChangeTracker() }
    single { SyncCoordinator(get(), get(), get()) }
    single { ConnectivityObserver(androidContext()) }
}
