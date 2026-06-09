package com.eahm.drinkwaterapp.core.backup

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.eahm.drinkwaterapp.backup.data.currentBackupSchedule
import com.eahm.drinkwaterapp.backup.data.observer.ConnectivityObserver
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Activates automatic background backup at app start. Three responsibilities, all guarded by the
 * "backup enabled" preference:
 *
 *  1. **Daily schedule** — (re)arms the daily auto-sync at the user's chosen hour. Also acts as the
 *     safety net that re-establishes the self-rescheduling chain if it was ever broken.
 *  2. **Foreground sync** — enqueues a sync when the app is brought to the foreground.
 *  3. **Reconnect flush** — when connectivity returns and a sync is pending, enqueues an on-change
 *     sync so deferred backups land as soon as the network is back ("postpone until internet").
 */
internal class BackupSyncInitializer(
    private val scheduler: BackupScheduler,
    private val prefs: BackupPreferences,
    private val connectivityObserver: ConnectivityObserver,
) : AppInitializer, DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun initialize() {
        scope.launch {
            if (prefs.getBackupEnabled()) {
                scheduler.scheduleAutoSync(prefs.currentBackupSchedule())
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // drop(1): ignore the observer's immediate current-state emission; we only want
        // the false → true transition (connectivity actually restored).
        scope.launch {
            connectivityObserver.isConnected.drop(1).collect { connected ->
                if (connected && prefs.getBackupEnabled() && prefs.getSyncPending()) {
                    scheduler.triggerOnChangeSync()
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        scope.launch {
            if (prefs.getBackupEnabled()) {
                scheduler.triggerForegroundSync()
            }
        }
    }
}
