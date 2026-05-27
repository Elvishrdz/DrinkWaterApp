package com.eahm.drinkwaterapp.backup.di

import com.eahm.drinkwaterapp.backup.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.BackupRepository
import com.eahm.drinkwaterapp.backup.BackupRepositoryImpl
import com.eahm.drinkwaterapp.backup.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.SyncBackupUseCase
import kotlinx.coroutines.sync.Mutex
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val backupCoreModule = module {
    single { Mutex() }

    singleOf(::BackupProviderRegistry)

    singleOf(::ConnectBackupUseCase)
    singleOf(::SyncBackupUseCase)
    singleOf(::RestoreBackupUseCase)
    singleOf(::CheckBackupStatusUseCase)
    singleOf(::DisconnectBackupUseCase)
    singleOf(::MigrateBackupUseCase)

    singleOf(::BackupRepositoryImpl) bind BackupRepository::class
}
