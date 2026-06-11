package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Reports every finished backup sync — from any trigger (manual, on-save, periodic, foreground,
 * reconnect, connect) — to analytics.
 *
 * Observes the process-wide [BackupSyncStatusHolder] instead of the Settings screen so background
 * worker syncs are counted even when no UI is open. Tracks each outcome instance exactly once:
 * [BackupSyncStatusHolder.onSkipped] clears the running flag without replacing the outcome, so a
 * plain running→idle transition check would re-report the previous outcome.
 */
internal class BackupSyncTracker(
    private val statusHolder: BackupSyncStatusHolder,
    private val backupPreferences: BackupPreferences,
    private val trackingService: TrackingService,
) : AppInitializer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun initialize() {
        scope.launch {
            var lastTracked: BackupSyncStatus.Outcome? = null
            statusHolder.status.collect { status ->
                val outcome = status.lastOutcome
                if (!status.running && outcome != null && outcome !== lastTracked) {
                    lastTracked = outcome
                    track(outcome)
                }
            }
        }
    }

    private suspend fun track(outcome: BackupSyncStatus.Outcome) {
        val provider = backupPreferences.getBackupProvider() ?: "none"
        val deviceCount = backupPreferences.getSeenDevices().size
        when (outcome) {
            is BackupSyncStatus.Outcome.Success -> trackingService.trackBackupSyncFinished(
                trigger = outcome.trigger.name.lowercase(),
                success = true,
                provider = provider,
                deviceCount = deviceCount,
            )

            is BackupSyncStatus.Outcome.Failure -> trackingService.trackBackupSyncFinished(
                trigger = outcome.trigger.name.lowercase(),
                success = false,
                provider = provider,
                deviceCount = deviceCount,
            )
        }
    }
}
