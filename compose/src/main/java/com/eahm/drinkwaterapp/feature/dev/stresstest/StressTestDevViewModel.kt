@file:Suppress("MagicNumber")

package com.eahm.drinkwaterapp.feature.dev.stresstest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class StressTestDevViewModel(
    database: AppDatabase,
) : ViewModel() {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    /**
     * KMP note: `Dispatchers.IO` is JVM/Android-only (internal on Kotlin/Native). When this
     * feature moves to `:shared`, swap this for the shared `kmpQueryContext` expect/actual.
     */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private val _state = MutableStateFlow(StressTestDevState.Data(anchorDate = today))
    val state = _state.asStateFlow()

    fun onAnchorDateChanged(date: LocalDate) = _state.update { it.copy(anchorDate = date) }
    fun onDaysChanged(days: Int) = _state.update { it.copy(days = days) }
    fun onGoalModeChanged(mode: GoalMode) = _state.update { it.copy(goalMode = mode) }
    fun onDefaultGoalChanged(ml: Int) = _state.update { it.copy(defaultGoalMl = ml) }
    fun onMinGoalChanged(ml: Int) = _state.update { it.copy(minGoalMl = ml) }
    fun onMaxGoalChanged(ml: Int) = _state.update { it.copy(maxGoalMl = ml) }
    fun onChangeEveryDaysChanged(days: Int) = _state.update { it.copy(changeEveryDays = days) }
    fun onRangeStartChanged(day: Int) = _state.update { it.copy(rangeStartDay = day) }
    fun onRangeEndChanged(day: Int) = _state.update { it.copy(rangeEndDay = day) }

    /** Generates [StressTestDevState.Data.days] of data backwards from the anchor date. */
    fun insertGenerated() {
        val current = _state.value
        generate(days = current.days.coerceIn(1, MAX_DAYS))
    }

    /** Inserts a fixed, predefined dataset (ignores the day/goal inputs). */
    fun insertPredefined() {
        generate(
            days = PREDEFINED_DAYS,
            goalMode = GoalMode.RANDOM,
            defaultGoalMl = 2000,
            minGoalMl = 1500,
            maxGoalMl = 3000,
            changeEveryDays = 30,
        )
    }

    private fun generate(
        days: Int,
        goalMode: GoalMode = _state.value.goalMode,
        defaultGoalMl: Int = _state.value.defaultGoalMl,
        minGoalMl: Int = _state.value.minGoalMl,
        maxGoalMl: Int = _state.value.maxGoalMl,
        changeEveryDays: Int = _state.value.changeEveryDays,
    ) {
        if (_state.value.isRunning) return
        val anchorDate = _state.value.anchorDate
        val oldestDate = anchorDate.minus(days - 1, DateTimeUnit.DAY)
        val seed = oldestDate.toEpochDays()

        viewModelScope.launch {
            val history = buildHistory(oldestDate, days, seed)
            val goals = buildGoals(
                oldestDate = oldestDate,
                days = days,
                goalMode = goalMode,
                defaultGoalMl = defaultGoalMl,
                minGoalMl = minGoalMl,
                maxGoalMl = maxGoalMl,
                changeEveryDays = changeEveryDays,
                seed = seed,
            )
            runJob(history, goals, "Generated $days days")
        }
    }

    fun clearAll() {
        if (_state.value.isRunning) return
        viewModelScope.launch {
            _state.update { it.copy(isRunning = true, resultMessage = "Clearing…") }
            withContext(ioDispatcher) {
                historyDao.deleteAll()
                goalDao.deleteAll()
            }
            _state.update {
                it.copy(
                    isRunning = false,
                    progress = 0,
                    total = 0,
                    resultMessage = "All history and goal records cleared",
                )
            }
        }
    }

    /** Builds 6–10 realistic drinks per day, deterministically seeded per day. */
    private fun buildHistory(oldestDate: LocalDate, days: Int, seed: Long): List<HistoryEntity> {
        val out = ArrayList<HistoryEntity>(days * AVG_DRINKS)
        repeat(days) { i ->
            val date = oldestDate.plusDays(i)
            val rng = Random(seed + i)
            val drinkCount = rng.nextInt(MIN_DRINKS, MAX_DRINKS + 1)
            val minutesUsed = HashSet<Int>()
            repeat(drinkCount) {
                // Spread across waking hours; avoid exact-minute collisions within a day.
                var totalMinutes = rng.nextInt(WAKE_START_HOUR * 60, WAKE_END_HOUR * 60)
                while (!minutesUsed.add(totalMinutes)) totalMinutes = (totalMinutes + 1) % (24 * 60)
                val amount = rng.nextInt(MIN_AMOUNT_STEPS, MAX_AMOUNT_STEPS + 1) * AMOUNT_STEP
                out += HistoryEntity(
                    amount = amount,
                    status = Status.IN_APP,
                    timestamp = LocalDateTime(
                        date,
                        LocalTime(totalMinutes / 60, totalMinutes % 60)
                    ),
                )
            }
        }
        return out
    }

    @Suppress("LongParameterList")
    private fun buildGoals(
        oldestDate: LocalDate,
        days: Int,
        goalMode: GoalMode,
        defaultGoalMl: Int,
        minGoalMl: Int,
        maxGoalMl: Int,
        changeEveryDays: Int,
        seed: Long,
    ): List<DailyGoalEntity> {
        val rng = Random(seed)
        val lo = minOf(minGoalMl, maxGoalMl)
        val hi = maxOf(minGoalMl, maxGoalMl)
        fun randomGoal() = (rng.nextInt(lo, hi + 1)).toFloat()

        // Invariant: a goal always exists at the oldest day (the app install date).
        val goals = ArrayList<DailyGoalEntity>()
        goals += DailyGoalEntity(
            effectiveDate = oldestDate,
            goalMl = if (goalMode == GoalMode.DEFAULT) defaultGoalMl.toFloat() else randomGoal(),
        )

        if (goalMode == GoalMode.DEFAULT) return goals

        val cadence = changeEveryDays.coerceAtLeast(1)
        val windowStart =
            if (goalMode == GoalMode.RANDOM_IN_RANGE) _state.value.rangeStartDay else 0
        val windowEnd =
            if (goalMode == GoalMode.RANDOM_IN_RANGE) _state.value.rangeEndDay else days - 1
        for (i in 1 until days) {
            if (i % cadence != 0) continue
            if (i < windowStart || i > windowEnd) continue
            goals += DailyGoalEntity(effectiveDate = oldestDate.plusDays(i), goalMl = randomGoal())
        }
        return goals
    }

    /** Clears existing data, then inserts history + goals in chunks while reporting progress. */
    private suspend fun runJob(
        history: List<HistoryEntity>,
        goals: List<DailyGoalEntity>,
        successLabel: String,
    ) {
        val total = history.size + goals.size
        _state.update {
            it.copy(
                isRunning = true,
                progress = 0,
                total = total,
                resultMessage = "Inserting…"
            )
        }
        withContext(ioDispatcher) {
            historyDao.deleteAll()
            goalDao.deleteAll()

            var done = 0
            history.chunked(CHUNK_SIZE).forEach { chunk ->
                historyDao.insertAll(*chunk.toTypedArray())
                done += chunk.size
                _state.update { it.copy(progress = done) }
            }
            goals.forEach { goal ->
                goalDao.insert(goal)
                done += 1
                _state.update { it.copy(progress = done) }
            }
        }
        _state.update {
            it.copy(
                isRunning = false,
                resultMessage = "$successLabel — ${history.size} drinks, ${goals.size} goals",
            )
        }
    }

    private fun LocalDate.plusDays(n: Int): LocalDate =
        minus(-n, DateTimeUnit.DAY)

    private companion object {
        const val MAX_DAYS = 5000
        const val PREDEFINED_DAYS = 90
        const val CHUNK_SIZE = 500
        const val MIN_DRINKS = 6
        const val MAX_DRINKS = 10
        const val AVG_DRINKS = 8
        const val WAKE_START_HOUR = 7
        const val WAKE_END_HOUR = 22
        const val AMOUNT_STEP = 50f
        const val MIN_AMOUNT_STEPS = 4 // 200 mL
        const val MAX_AMOUNT_STEPS = 8 // 400 mL
    }
}
