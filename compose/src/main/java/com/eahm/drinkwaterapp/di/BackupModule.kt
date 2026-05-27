package com.eahm.drinkwaterapp.di

import androidx.work.WorkManager
import com.eahm.drinkwaterapp.backup.BackupDataSource
import com.eahm.drinkwaterapp.backup.BackupPreferences
import com.eahm.drinkwaterapp.backup.BackupScheduler
import com.eahm.drinkwaterapp.backup.CloudBackupWorker
import com.eahm.drinkwaterapp.backup.DropboxBackupProvider
import com.eahm.drinkwaterapp.backup.GoogleDriveBackupProvider
import com.eahm.drinkwaterapp.backup.di.backupCoreModule
import com.eahm.drinkwaterapp.core.backup.BackupSerializer
import com.eahm.drinkwaterapp.core.backup.WorkManagerBackupScheduler
import com.eahm.drinkwaterapp.core.data.datastore.DataStorePrefsStorage
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
    single { GoogleDriveBackupProvider(get(), get(backupJsonQualifier)) }
    single { DropboxBackupProvider(get(backupJsonQualifier)) }

    // Bridge implementations
    single { BackupSerializer(get(), get(), get(backupJsonQualifier)) } bind BackupDataSource::class
    singleOf(::WorkManagerBackupScheduler) bind BackupScheduler::class

    // BackupPreferences bridge — reuse the DataStorePrefsStorage single
    single<BackupPreferences> { get<DataStorePrefsStorage>() }

    // WorkManager worker
    workerOf(::CloudBackupWorker)
}
