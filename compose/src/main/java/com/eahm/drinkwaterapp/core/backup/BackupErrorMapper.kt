package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.backup.BackupError
import com.eahm.drinkwaterapp.core.presentation.UiText

object BackupErrorMapper {
    fun toUiText(error: BackupError): UiText = when (error) {
        is BackupError.NotSignedIn ->
            UiText.DynamicString("Not signed in")

        is BackupError.NetworkUnavailable ->
            UiText.DynamicString("Network unavailable")

        is BackupError.UploadFailed ->
            UiText.DynamicString("Upload failed")

        is BackupError.DownloadFailed ->
            UiText.DynamicString("Download failed")

        is BackupError.IncompatibleVersion ->
            UiText.DynamicString("Incompatible backup version (${error.remoteSchema})")

        is BackupError.MigrationFailed ->
            UiText.DynamicString("Migration failed")

        is BackupError.AuthCancelled ->
            UiText.DynamicString("Authentication cancelled")

        is BackupError.AuthFailed ->
            UiText.DynamicString("Authentication failed")

        is BackupError.QuotaExceeded ->
            UiText.DynamicString("Storage quota exceeded")

        is BackupError.NoProviderSelected ->
            UiText.DynamicString("No backup provider selected")

        is BackupError.FileNotFound ->
            UiText.DynamicString("Backup file not found")

        is BackupError.SameProviderMigration ->
            UiText.DynamicString("Cannot migrate to the same provider")

        is BackupError.Unknown ->
            UiText.DynamicString("Unknown error")

        is BackupError.MergeCorrupted ->
            UiText.DynamicString("Merge failed: remote backup data is corrupted")
    }
}
