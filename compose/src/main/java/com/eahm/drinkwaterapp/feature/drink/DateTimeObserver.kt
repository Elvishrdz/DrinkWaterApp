package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

// TODO: make it lifecycle aware to not keep it alive when app is in background
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
    // distinctUntilChangedBy ensures only a real day boundary triggers downstream work.
    val currentDay: StateFlow<LocalDateTime> = callbackFlow {

        // initial
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

        application.registerReceiver(receiver, filter)

        awaitClose { application.unregisterReceiver(receiver) }
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
