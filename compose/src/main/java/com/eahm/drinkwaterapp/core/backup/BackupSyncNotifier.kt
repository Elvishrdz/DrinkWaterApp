package com.eahm.drinkwaterapp.core.backup

import android.content.Context
import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
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
 * Dev-only for now: gated behind a persisted flag toggled from the Backup dev screen. When the
 * flag is on, the notification text updates as the background worker runs; when off (or when the
 * sync goes idle without a recent outcome), the notification is cleared.
 */
internal class BackupSyncNotifier(
    private val context: Context,
    private val statusHolder: BackupSyncStatusHolder,
    private val prefs: PrefsStorage,
) : AppInitializer {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val enabled = MutableStateFlow(false)

    override fun initialize() {
        BackupSyncNotificationChannel.createChannel(context)

        scope.launch {
            enabled.value = prefs.getDevBackupNotificationEnabled()

            combine(enabled, statusHolder.status) { on, status -> on to status }
                .collect { (on, status) ->
                    if (on) render(status) else BackupSyncNotification.cancel(context)
                }
        }
    }

    /** Toggle the dev notification. Persists the choice and applies it immediately. */
    suspend fun setEnabled(value: Boolean) {
        prefs.setDevBackupNotificationEnabled(value)
        enabled.value = value
    }

    suspend fun isEnabled(): Boolean = prefs.getDevBackupNotificationEnabled()

    private fun render(status: BackupSyncStatus) {
        when {
            status.running -> BackupSyncNotification.show(
                context = context,
                title = "Backing up…",
                text = "Sync in progress (${status.activeTrigger?.name ?: "AUTO"})",
                ongoing = true,
            )

            else -> when (val outcome = status.lastOutcome) {
                is BackupSyncStatus.Outcome.Success -> BackupSyncNotification.show(
                    context = context,
                    title = "Backup complete",
                    text = "Last backup ${formatTime(outcome.syncedAt)} (${outcome.trigger.name})",
                    ongoing = false,
                )

                is BackupSyncStatus.Outcome.Failure -> BackupSyncNotification.show(
                    context = context,
                    title = "Backup failed",
                    text = "${outcome.trigger.name} → ${outcome.disposition.name}",
                    ongoing = false,
                )

                null -> BackupSyncNotification.cancel(context)
            }
        }
    }

    private fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
}
