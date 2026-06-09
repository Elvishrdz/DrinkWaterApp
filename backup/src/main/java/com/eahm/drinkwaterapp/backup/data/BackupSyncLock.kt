package com.eahm.drinkwaterapp.backup.data

import kotlinx.coroutines.sync.Mutex

/**
 * Dedicated lock shared by the backup sync paths (foreground sync, background worker, provider
 * migration) to guarantee only one mutates the backup at a time.
 *
 * Wrapping the [Mutex] in a named type keeps it out of the DI graph as a bare `Mutex`, so an
 * unrelated subsystem injecting a `Mutex` can never accidentally collide with the backup lock.
 */
class BackupSyncLock {
    val mutex = Mutex()
}
