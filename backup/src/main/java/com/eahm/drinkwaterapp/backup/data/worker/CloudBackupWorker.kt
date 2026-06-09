package com.eahm.drinkwaterapp.backup.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eahm.drinkwaterapp.backup.data.BackupChangeTracker
import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.data.currentBackupSchedule
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.SyncDisposition
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CloudBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val syncUseCase: SyncBackupUseCase by inject()
    private val statusHolder: BackupSyncStatusHolder by inject()
    private val prefs: BackupPreferences by inject()
    private val registry: BackupProviderRegistry by inject()
    private val scheduler: BackupScheduler by inject()
    private val changeTracker: BackupChangeTracker by inject()

    override suspend fun doWork(): Result {
        val trigger = resolveTrigger()
        // Capture before syncing so we can tell if a write lands mid-run.
        val changeSnapshot = changeTracker.snapshot()

        val result = runSync(trigger)

        // The auto-backup is implemented as self-rescheduling one-time work: once it reaches a
        // terminal state (not a retry), re-arm the next run using the latest persisted schedule
        // (fixed daily hour or interval). WorkManager handles the retry case itself, so we must
        // not reschedule then or we'd stack jobs.
        if (trigger == SyncTrigger.PERIODIC && result != Result.retry()) {
            scheduler.scheduleAutoSync(prefs.currentBackupSchedule())
        }

        // If a change landed during this run, enqueue exactly one follow-up on-change sync so the
        // newest data is uploaded. Only on a completed run (not a retry — that re-runs anyway).
        if (result != Result.retry() && changeTracker.hasChangedSince(changeSnapshot)) {
            scheduler.enqueueFollowUpOnChangeSync()
        }
        return result
    }

    private suspend fun runSync(trigger: SyncTrigger): Result {
        val provider = resolveProvider() ?: return Result.failure()

        statusHolder.onStarted(trigger)
        return when (val result = syncUseCase(provider)) {
            is BackupResult.Success -> {
                if (result.value is SyncResult.AlreadyInProgress) {
                    // Another sync holds the lock — don't clobber its status.
                    statusHolder.onSkipped()
                } else {
                    statusHolder.onSuccess(trigger, System.currentTimeMillis())
                }
                Result.success()
            }

            is BackupResult.Failure -> {
                val error = result.error
                val retryable =
                    error is BackupError.NetworkUnavailable || error is BackupError.Unknown
                val willRetry = retryable && runAttemptCount < MAX_RETRIES
                val disposition = when {
                    error is BackupError.NetworkUnavailable -> SyncDisposition.WAITING_FOR_CONNECTION
                    willRetry -> SyncDisposition.WILL_RETRY
                    else -> SyncDisposition.FAILED
                }
                statusHolder.onFailure(trigger, error, disposition)
                if (willRetry) Result.retry() else Result.failure()
            }
        }
    }

    private fun resolveTrigger(): SyncTrigger {
        val name = inputData.getString(KEY_TRIGGER) ?: return SyncTrigger.PERIODIC
        return runCatching { SyncTrigger.valueOf(name) }.getOrDefault(SyncTrigger.PERIODIC)
    }

    private suspend fun resolveProvider(): CloudBackupProvider? {
        val id = prefs.getBackupProvider() ?: return null
        val type = CloudProviderType.fromId(id) ?: return null
        return registry.get(type)
    }

    companion object {
        const val WORK_NAME_PERIODIC = "BACKUP_SYNC_WORK_PERIODIC"
        const val WORK_NAME_MANUAL = "BACKUP_SYNC_WORK_MANUAL"
        const val WORK_NAME_ON_CHANGE = "cloud_backup_sync_on_change"
        const val WORK_NAME_FOREGROUND = "cloud_backup_sync_foreground"
        const val KEY_TRIGGER = "backup_sync_trigger"
        private const val MAX_RETRIES = 3
    }
}
