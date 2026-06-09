package com.eahm.drinkwaterapp.core.backup

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.eahm.drinkwaterapp.backup.data.worker.CloudBackupWorker
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.BackupSchedule
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkManagerBackupScheduler(
    private val workManager: WorkManager,
) : BackupScheduler {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun scheduleAutoSync(schedule: BackupSchedule) {
        val initialDelayMs = when (schedule) {
            is BackupSchedule.Daily -> millisUntilNext(schedule.hourOfDay)
            is BackupSchedule.EveryHours ->
                TimeUnit.HOURS.toMillis(schedule.hours.coerceAtLeast(1).toLong())
        }

        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .setInputData(triggerData(SyncTrigger.PERIODIC))
            .build()

        // REPLACE so changing the schedule reschedules the pending job instead of stacking jobs.
        // (ExistingWorkPolicy has no UPDATE — that's periodic-only.)
        workManager.enqueueUniqueWork(
            CloudBackupWorker.WORK_NAME_PERIODIC,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun triggerManualSync() {
        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .setInputData(triggerData(SyncTrigger.MANUAL))
            .build()

        workManager.enqueueUniqueWork(
            CloudBackupWorker.WORK_NAME_MANUAL,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun triggerOnChangeSync() {
        // KEEP: while an on-change sync already exists (running or enqueued), drop this one — the
        // worker's own follow-up (enqueueFollowUpOnChangeSync) is what guarantees mid-run changes
        // get synced, so the queue can never grow beyond "one running + one queued".
        enqueueOnChange(ExistingWorkPolicy.KEEP)
    }

    override fun enqueueFollowUpOnChangeSync() {
        // Called from inside a still-RUNNING worker; APPEND_OR_REPLACE queues exactly one follow-up
        // behind it rather than being dropped by KEEP.
        enqueueOnChange(ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    private fun enqueueOnChange(policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(networkConstraints)
            .setInputData(triggerData(SyncTrigger.ON_SAVE))
            .build()

        workManager.enqueueUniqueWork(
            CloudBackupWorker.WORK_NAME_ON_CHANGE,
            policy,
            request,
        )
    }

    override fun triggerForegroundSync() {
        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(networkConstraints)
            .setInputData(triggerData(SyncTrigger.FOREGROUND))
            .build()

        workManager.enqueueUniqueWork(
            CloudBackupWorker.WORK_NAME_FOREGROUND,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    override fun cancelAll() {
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_MANUAL)
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_ON_CHANGE)
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_FOREGROUND)
    }

    private fun triggerData(trigger: SyncTrigger) =
        workDataOf(CloudBackupWorker.KEY_TRIGGER to trigger.name)

    /** Milliseconds from now until the next occurrence of [hourOfDay] (today if still ahead). */
    private fun millisUntilNext(hourOfDay: Int): Long {
        val hour = hourOfDay.coerceIn(MIN_HOUR, MAX_HOUR)
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }

    private companion object {
        const val MIN_HOUR = 0
        const val MAX_HOUR = 23
    }
}
