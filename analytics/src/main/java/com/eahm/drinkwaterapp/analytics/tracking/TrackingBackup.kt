package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingBackup {

    /**
     * User connected a cloud backup provider (OAuth completed or re-connect).
     * [provider] is the CloudProviderType id ("google_drive" / "dropbox") — identifies the most
     * and least used provider. [deviceCount] is the number of devices seen on the backup so far,
     * to measure multi-device usage.
     */
    fun trackBackupConnected(provider: String, deviceCount: Int)

    /** User disconnected the backup. [provider] is the provider that was disconnected. */
    fun trackBackupDisconnected(provider: String)

    /** User tapped the manual "Sync now" button. Measures manual sync usage. */
    fun trackBackupManualSyncTapped(provider: String)

    /** User tapped restore-from-backup (replace local data with the cloud copy). */
    fun trackBackupRestoreTapped(provider: String)

    /**
     * User changed the auto-backup schedule. [mode] is "daily" or "interval"; [dailyHour] is set
     * only for daily mode (most common auto-backup hour), [intervalHours] only for interval mode
     * (how often users want to back up).
     */
    fun trackBackupScheduleChanged(mode: String, dailyHour: Int?, intervalHours: Int?)

    /** User toggled sync-on-save (automatically back up every new change). */
    fun trackBackupSyncOnSaveToggled(enabled: Boolean)

    /**
     * A sync finished, from any source. [trigger] is the lowercase SyncTrigger name (manual,
     * on_save, periodic, foreground, reconnect, connect). Time between these events per user
     * gives the average real backup frequency; [deviceCount] measures multi-device usage.
     */
    fun trackBackupSyncFinished(
        trigger: String,
        success: Boolean,
        provider: String,
        deviceCount: Int,
    )

    companion object {
        const val EVENT_BACKUP_CONNECTED = "backup_connected"
        const val EVENT_BACKUP_DISCONNECTED = "backup_disconnected"
        const val EVENT_BACKUP_MANUAL_SYNC_TAPPED = "backup_manual_sync_tapped"
        const val EVENT_BACKUP_RESTORE_TAPPED = "backup_restore_tapped"
        const val EVENT_BACKUP_SCHEDULE_CHANGED = "backup_schedule_changed"
        const val EVENT_BACKUP_SYNC_ON_SAVE_TOGGLED = "backup_sync_on_save_toggled"
        const val EVENT_BACKUP_SYNC_FINISHED = "backup_sync_finished"
    }
}
