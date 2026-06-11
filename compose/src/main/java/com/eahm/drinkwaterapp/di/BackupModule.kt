package com.eahm.drinkwaterapp.di

import androidx.work.WorkManager
import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.provider.DropboxBackupProvider
import com.eahm.drinkwaterapp.backup.data.provider.GoogleDriveBackupProvider
import com.eahm.drinkwaterapp.backup.data.worker.CloudBackupWorker
import com.eahm.drinkwaterapp.backup.di.backupCoreModule
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.core.backup.BackupSerializer
import com.eahm.drinkwaterapp.core.backup.BackupSyncInitializer
import com.eahm.drinkwaterapp.core.backup.BackupSyncNotifier
import com.eahm.drinkwaterapp.core.backup.BackupSyncTracker
import com.eahm.drinkwaterapp.core.backup.WorkManagerBackupScheduler
import com.eahm.drinkwaterapp.core.data.datastore.DataStorePrefsStorage
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val backupJsonQualifier = named("backup_json")

internal val backupModule = module {
    includes(backupCoreModule)

    // Serialization (qualified to avoid conflict with any app-level Json)
    single<Json>(backupJsonQualifier) {
        Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }

    // Ktor HttpClient for Drive REST API
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(get(backupJsonQualifier))
            }
        }
    }

    // WorkManager
    single { WorkManager.getInstance(get()) }

    // Provider implementations — explicit factories to inject qualified Json
    single { GoogleDriveBackupProvider(get(), get(), get(backupJsonQualifier)) }
    single { DropboxBackupProvider(get(), get(backupJsonQualifier)) }

    // Registry — explicit to avoid Koin ambiguity (two CloudBackupProvider params)
    single {
        BackupProviderRegistry(
            get<GoogleDriveBackupProvider>(),
            get<DropboxBackupProvider>()
        )
    }

    // Bridge implementations
    single {
        BackupSerializer(get(), get(), get(), get(), get(backupJsonQualifier))
    } bind BackupDataSource::class
    singleOf(::WorkManagerBackupScheduler) bind BackupScheduler::class

    // Activates daily auto-sync + foreground/reconnect triggers at app start.
    singleOf(::BackupSyncInitializer) bind AppInitializer::class

    // Dev-only: mirrors background sync status into a dedicated notification.
    singleOf(::BackupSyncNotifier) bind AppInitializer::class

    // Reports every finished sync (any trigger) to analytics.
    singleOf(::BackupSyncTracker) bind AppInitializer::class

    // BackupPreferences bridge — reuse the DataStorePrefsStorage single
    single<BackupPreferences> { get<DataStorePrefsStorage>() }

    // WorkManager worker
    workerOf(::CloudBackupWorker)
}
