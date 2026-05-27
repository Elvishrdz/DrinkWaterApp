package com.eahm.drinkwaterapp.backup

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import java.security.MessageDigest

class SyncBackupUseCase(
    private val mutex: Mutex,
    private val dataSource: BackupDataSource,
    private val prefs: BackupPreferences,
) {
    suspend operator fun invoke(
        context: Context?,
        provider: CloudBackupProvider,
    ): BackupResult<SyncResult> {
        if (!mutex.tryLock()) return BackupResult.Success(SyncResult.AlreadyInProgress)
        return try {
            val serialized = dataSource.serialize()
            val newHash = sha256(serialized)
            val storedHash = prefs.getBackupContentHash()

            if (newHash == storedHash) {
                return BackupResult.Success(SyncResult.SkippedNoChanges)
            }

            when (val uploadResult = provider.upload(context, serialized)) {
                is BackupResult.Success -> {
                    val now = System.currentTimeMillis()
                    prefs.setBackupContentHash(newHash)
                    prefs.setBackupLastSyncAt(now)
                    prefs.setBackupLastError(null)
                    BackupResult.Success(SyncResult.Uploaded)
                }

                is BackupResult.Failure -> {
                    prefs.setBackupLastError(uploadResult.error.javaClass.simpleName)
                    uploadResult
                }
            }
        } finally {
            mutex.unlock()
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
