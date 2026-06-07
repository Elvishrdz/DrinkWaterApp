package com.eahm.drinkwaterapp.backup

sealed interface BackupError {
    data object NotSignedIn : BackupError
    data object NetworkUnavailable : BackupError
    data class UploadFailed(val cause: Throwable? = null) : BackupError
    data class DownloadFailed(val cause: Throwable? = null) : BackupError
    data class IncompatibleVersion(val remoteSchema: Int) : BackupError
    data class MigrationFailed(val cause: Throwable? = null) : BackupError
    data object AuthCancelled : BackupError
    data object AuthFailed : BackupError
    data class QuotaExceeded(val fileSizeBytes: Long = 0L) : BackupError
    data object NoProviderSelected : BackupError
    data object FileNotFound : BackupError
    data object SameProviderMigration : BackupError
    data class Unknown(val cause: Throwable? = null) : BackupError
    data object MergeCorrupted : BackupError
}
