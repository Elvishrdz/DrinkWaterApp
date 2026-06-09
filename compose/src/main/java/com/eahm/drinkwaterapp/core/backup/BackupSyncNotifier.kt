package com.eahm.drinkwaterapp.core.backup

import android.content.Context
import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.systemnotifications.channel.BackupSyncNotificationChannel
import com.eahm.drinkwaterapp.core.data.systemnotifications.notification.BackupSyncNotification
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mirrors the unified backup sync status into a dedicated, updating notification.
 *
 * Dev-only for now: gated behind a persisted [BackupNotificationMode] toggled from the Backup dev
 * screen. The notification updates as the sync runs and is cleared when the active mode says the
 * current trigger shouldn't be shown (or when the sync goes idle without a recent outcome).
 */
internal class BackupSyncNotifier(
    private val context: Context,
    private val statusHolder: BackupSyncStatusHolder,
    private val prefs: PrefsStorage,
) : AppInitializer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mode = MutableStateFlow(BackupNotificationMode.OFF)

    override fun initialize() {
        BackupSyncNotificationChannel.createChannel(context)

        scope.launch {
            mode.value = BackupNotificationMode.fromName(prefs.getDevBackupNotificationModeName())

            combine(mode, statusHolder.status) { mode, status -> mode to status }
                .collect { (mode, status) -> render(mode, status) }
        }
    }

    /** Set the dev notification mode. Persists the choice and applies it immediately. */
    suspend fun setMode(value: BackupNotificationMode) {
        prefs.setDevBackupNotificationModeName(value.name)
        mode.value = value
    }

    suspend fun currentMode(): BackupNotificationMode =
        BackupNotificationMode.fromName(prefs.getDevBackupNotificationModeName())

    private fun render(mode: BackupNotificationMode, status: BackupSyncStatus) {
        val trigger = status.currentTrigger()
        if (trigger == null || !mode.shouldNotify(trigger)) {
            BackupSyncNotification.cancel(context)
            return
        }

        when {
            status.running -> BackupSyncNotification.show(
                context = context,
                title = "Backing up…",
                text = "Sync in progress (${trigger.name})",
                ongoing = true,
            )

            else -> when (val outcome = status.lastOutcome) {
                is BackupSyncStatus.Outcome.Success -> BackupSyncNotification.show(
                    context = context,
                    title = "Backup complete",
                    text = "Last backup ${formatTime(outcome.syncedAt)} (${trigger.name})",
                    ongoing = false,
                )

                is BackupSyncStatus.Outcome.Failure -> BackupSyncNotification.show(
                    context = context,
                    title = "Backup failed",
                    text = "${trigger.name} → ${outcome.disposition.name}",
                    ongoing = false,
                )

                null -> BackupSyncNotification.cancel(context)
            }
        }
    }

    /** The trigger that the current notification should reflect — the running one, else the last. */
    private fun BackupSyncStatus.currentTrigger(): SyncTrigger? = when {
        running -> activeTrigger
        else -> when (val outcome = lastOutcome) {
            is BackupSyncStatus.Outcome.Success -> outcome.trigger
            is BackupSyncStatus.Outcome.Failure -> outcome.trigger
            null -> null
        }
    }

    private fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
}
