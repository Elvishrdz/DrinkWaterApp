package com.eahm.drinkwaterapp.backup.domain.model

/**
 * Describes a backup that already exists on the provider at first-connection time.
 * Surfaced to the user so they can decide whether to keep the remote data or replace
 * it with this device's data. Carries the remote [metadata] for display (created date,
 * originating device, app version).
 */
data class ExistingRemoteBackup(
    val metadata: BackupMetadata,
)
