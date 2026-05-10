package com.eahm.drinkwaterapp.core.data.streak

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.feature.drink.DateTimeZoneChangedObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val streakModule = module { singleOf(::StreakRepository) }

@OptIn(ExperimentalCoroutinesApi::class)
class StreakRepository(
    database: AppDatabase,
    private val prefsStorage: PrefsStorage,
    dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
) {
    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    private data class ComputedStreaks(
        val currentGoalStreak: Int,
        val currentDrinkStreak: Int,
        val bestGoalStreak: Int,
        val bestDrinkStreak: Int,
        val personalBestMl: Int,
    )

    private val computedFromDb: Flow<ComputedStreaks> = dateTimeZoneChangedObserver.currentDateTime
        .flatMapLatest { now ->
            val today = now.date
            combine(
                historyDao.getAll(),
                goalDao.getGoalForDate(today.toEpochDays()),
            ) { records, goalEntity ->
                computeStreaks(today, records, goalEntity?.goalMl ?: DEFAULT_GOAL_ML)
            }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            computedFromDb.collect { computed ->
                if (computed.bestGoalStreak > prefsStorage.getBestGoalStreak().first())
                    prefsStorage.setBestGoalStreak(computed.bestGoalStreak)
                if (computed.bestDrinkStreak > prefsStorage.getBestDrinkStreak().first())
                    prefsStorage.setBestDrinkStreak(computed.bestDrinkStreak)
            }
        }
    }

    val streakData: Flow<StreakData> = combine(
        computedFromDb,
        prefsStorage.getBestGoalStreak(),
        prefsStorage.getBestDrinkStreak(),
    ) { computed, bestGoal, bestDrink ->
        StreakData(
            currentGoalStreak = computed.currentGoalStreak,
            currentDrinkStreak = computed.currentDrinkStreak,
            bestGoalStreak = maxOf(computed.bestGoalStreak, bestGoal),
            bestDrinkStreak = maxOf(computed.bestDrinkStreak, bestDrink),
            personalBestMl = computed.personalBestMl,
        )
    }

    private fun computeStreaks(
        today: LocalDate,
        records: List<HistoryEntity>,
        goalThreshold: Float,
    ): ComputedStreaks {
        val dailyTotals: Map<LocalDate, Float> = records
            .filter { it.amount > 0f }
            .groupBy { it.timestamp.date }
            .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }

        // Current goal streak: walk backwards from today
        var currentGoalStreak = 0
        var date = today
        for (i in 0 until 3650) {
            val total = dailyTotals[date] ?: 0f
            if (total >= goalThreshold) {
                currentGoalStreak++
                date = date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }

        // Current drink streak: walk backwards from today
        var currentDrinkStreak = 0
        date = today
        for (i in 0 until 3650) {
            val total = dailyTotals[date] ?: 0f
            if (total > 0f) {
                currentDrinkStreak++
                date = date.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }

        // Best streaks: forward scan over all recorded dates
        val sortedDates = dailyTotals.keys.sorted()
        var bestGoalStreak = 0
        var bestDrinkStreak = 0
        var runGoal = 0
        var runDrink = 0
        var prevDate: LocalDate? = null
        for (d in sortedDates) {
            if (prevDate != null && d != prevDate.plus(1, DateTimeUnit.DAY)) {
                runGoal = 0
                runDrink = 0
            }
            val total = dailyTotals[d] ?: 0f
            if (total >= goalThreshold) {
                runGoal++
                if (runGoal > bestGoalStreak) bestGoalStreak = runGoal
            } else {
                runGoal = 0
            }
            if (total > 0f) {
                runDrink++
                if (runDrink > bestDrinkStreak) bestDrinkStreak = runDrink
            } else {
                runDrink = 0
            }
            prevDate = d
        }

        val personalBestMl = dailyTotals.values.maxOrNull()?.toInt() ?: 0

        return ComputedStreaks(
            currentGoalStreak = currentGoalStreak,
            currentDrinkStreak = currentDrinkStreak,
            bestGoalStreak = bestGoalStreak,
            bestDrinkStreak = bestDrinkStreak,
            personalBestMl = personalBestMl,
        )
    }
}
