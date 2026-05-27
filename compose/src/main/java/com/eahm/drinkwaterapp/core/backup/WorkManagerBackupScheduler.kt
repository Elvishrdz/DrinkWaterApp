package com.eahm.drinkwaterapp.core.backup

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eahm.drinkwaterapp.backup.BackupScheduler
import com.eahm.drinkwaterapp.backup.CloudBackupWorker
import java.util.concurrent.TimeUnit

class WorkManagerBackupScheduler(
    private val workManager: WorkManager,
) : BackupScheduler {

    override fun scheduleAutoSync(intervalDays: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(
            intervalDays.toLong(),
            TimeUnit.DAYS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CloudBackupWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override fun triggerManualSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            CloudBackupWorker.WORK_NAME_MANUAL,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override fun cancelAll() {
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_PERIODIC)
        workManager.cancelUniqueWork(CloudBackupWorker.WORK_NAME_MANUAL)
    }
}
