package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger

/**
 * Dev-only setting controlling when the dedicated backup-status notification is shown. Selecting a
 * mode is mutually exclusive — picking one clears the others; toggling the active one off → [OFF].
 */
enum class BackupNotificationMode {
    /** Never show the notification. */
    OFF,

    /** Show only for background/automatic backups (anything that isn't an on-save write). */
    BACKGROUND_ONLY,

    /** Show only for on-save backups (the debounced data-write sync). */
    ON_SAVE_ONLY,

    /** Show for every backup activity, background or foreground. */
    ALL;

    /** Whether a backup driven by [trigger] should surface a notification under this mode. */
    fun shouldNotify(trigger: SyncTrigger): Boolean = when (this) {
        OFF -> false
        ALL -> true
        ON_SAVE_ONLY -> trigger == SyncTrigger.ON_SAVE
        BACKGROUND_ONLY -> trigger != SyncTrigger.ON_SAVE
    }

    companion object {
        fun fromName(name: String?): BackupNotificationMode =
            entries.firstOrNull { it.name == name } ?: OFF
    }
}
