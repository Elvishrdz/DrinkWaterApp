package com.eahm.drinkwaterapp.feature.drink

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val clockTickObserverModule = module {
    singleOf(::ClockTickObserver)
}

/**
 * Controls how often [ClockTickObserver.observe] emits and how two values are considered equal.
 *
 * [tickIntervalMs] is intentionally smaller than the granularity period so the observer
 * catches the exact boundary without busy-waiting.
 *
 * | Granularity | Tick rate | Max emission lag |
 * |-------------|-----------|-----------------|
 * | YEAR        | 1 min     | ~1 min          |
 * | MONTH       | 1 min     | ~1 min          |
 * | DAY         | 30 s      | ~30 s           |
 * | HOUR        | 5 s       | ~5 s            |
 * | MINUTE      | 1 s       | ~1 s            |
 * | SECOND      | 500 ms    | ~500 ms         |
 * | MILLIS      | 1 ms      | ~1 ms (expensive — dev/test use only) |
 */
enum class ClockGranularity(internal val tickIntervalMs: Long) {
    YEAR(60_000L),
    MONTH(60_000L),
    DAY(30_000L),
    HOUR(5_000L),
    MINUTE(1_000L),
    SECOND(500L),
    MILLIS(1L),
}

/**
 * Ticker-based date/time observer with configurable emission granularity.
 *
 * Returns a cold [Flow] that:
 * - only ticks while the app is foregrounded (>= [Lifecycle.State.STARTED])
 * - emits the current [LocalDateTime] immediately on subscribe
 * - re-emits only when the component defined by [ClockGranularity] actually changes
 *
 * Usage:
 * ```
 * clockTickObserver.observe(ClockGranularity.MINUTE)   // emit on each new minute
 * clockTickObserver.observe(ClockGranularity.HOUR)     // emit on each new hour
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ClockTickObserver {

    fun observe(granularity: ClockGranularity): Flow<LocalDateTime> =
        ProcessLifecycleOwner.get()
            .lifecycle
            .currentStateFlow
            .flatMapLatest { state ->
                if (state.isAtLeast(Lifecycle.State.STARTED)) {
                    flow {
                        while (true) {
                            emit(getCurrentDateTime())
                            delay(granularity.tickIntervalMs)
                        }
                    }
                } else {
                    emptyFlow()
                }
            }
            .distinctUntilChangedBy { it.toGranularityKey(granularity) }

    private fun getCurrentDateTime(): LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

private fun LocalDateTime.toGranularityKey(granularity: ClockGranularity): Long =
    when (granularity) {
        ClockGranularity.YEAR ->
            year.toLong()

        ClockGranularity.MONTH ->
            year * 100L + monthNumber

        ClockGranularity.DAY ->
            year * 10_000L + monthNumber * 100L + dayOfMonth

        ClockGranularity.HOUR ->
            year * 1_000_000L + monthNumber * 10_000L + dayOfMonth * 100L + hour

        ClockGranularity.MINUTE ->
            year * 100_000_000L + monthNumber * 1_000_000L + dayOfMonth * 10_000L + hour * 100L + minute

        ClockGranularity.SECOND ->
            year * 10_000_000_000L + monthNumber * 100_000_000L + dayOfMonth * 1_000_000L +
                    hour * 10_000L + minute * 100L + second

        ClockGranularity.MILLIS ->
            year * 1_000_000_000_000L + monthNumber * 10_000_000_000L + dayOfMonth * 100_000_000L +
                    hour * 1_000_000L + minute * 10_000L + second * 1_000L + nanosecond / 1_000_000
    }
