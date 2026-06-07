package com.eahm.drinkwaterapp.backup

interface BackupScheduler {
    fun scheduleAutoSync(intervalDays: Int = 1)
    fun triggerManualSync()
    fun cancelAll()

    /** Enqueue a debounced one-time sync triggered by a data write. Uses KEEP policy — ignored if one is already queued. */
    fun triggerOnChangeSync()

    /** Enqueue a one-time foreground sync (app came to foreground). Uses KEEP policy. */
    fun triggerForegroundSync()
}
