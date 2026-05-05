package com.eahm.drinkwaterapp.system.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Can take more than 2 minute to be executed by the system
// Executed after phone is fully ready
class BootCompletedReceiver : BroadcastReceiver() {

    // IMPORTANT: this broadcast is not only sent after the device boots but also delivered to an app
    // when it is removed from the "Stopped" state and the user is unlocked, such as the first launch
    // after force-stopping the app.
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            println("Drink water app: Booted completed")
        }
    }
}
