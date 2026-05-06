package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val dateTimeObserverModule = module {
    singleOf(::DateTimeObserver)
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DateTimeObserver(
    val application: Application,
) {
    private val job = SupervisorJob()

    private val globalExceptionHandler = CoroutineExceptionHandler { _, _ ->
        // log exceptions if required
    }

    private val scope = CoroutineScope(job + Dispatchers.IO + globalExceptionHandler)

    // Emits the current LocalDateTime and re-emits whenever the calendar date changes
    // (midnight rollover, manual date/time edit, or timezone switch).
    // The BroadcastReceiver is only registered while the app is foregrounded (>= STARTED),
    // so it is automatically unregistered in the background — no battery wasted.
    // When the app returns to the foreground the flow restarts and emits the current date;
    // distinctUntilChangedBy ensures only a real day boundary triggers downstream work.
    val currentDateTime: StateFlow<LocalDateTime> = ProcessLifecycleOwner.get()
        .lifecycle
        .currentStateFlow
        .flatMapLatest { state ->
            if (state.isAtLeast(Lifecycle.State.STARTED)) {
                callbackFlow {
                    trySend(getCurrentDateTime())

                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            trySend(getCurrentDateTime())
                        }
                    }

                    val filter = IntentFilter().apply {
                        addAction(Intent.ACTION_DATE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                    }

                    ContextCompat.registerReceiver(
                        application,
                        receiver,
                        filter,
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )

                    awaitClose { application.unregisterReceiver(receiver) }
                }
            } else {
                emptyFlow()
            }
        }
        .distinctUntilChangedBy { it.date }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = getCurrentDateTime(),
        )

    private fun getCurrentDateTime(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}
