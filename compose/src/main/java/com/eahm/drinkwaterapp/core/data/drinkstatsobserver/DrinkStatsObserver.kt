package com.eahm.drinkwaterapp.core.data.drinkstatsobserver

import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.feature.drink.DateTimeZoneChangedObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val drinkStatsObserverModule = module {
    singleOf(::DrinkStatsObserver)
}

class DrinkStatsObserver(
    val database: AppDatabase,
    dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
) {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentDrinkAmount = dateTimeZoneChangedObserver.currentDateTime
        .flatMapLatest { day ->
            historyDao.getTotalDrinkAmount(start = day.toStartOfDayTime, end = day.toEndOfDayTime)
        }
        .flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentGoalAmount = dateTimeZoneChangedObserver.currentDateTime
        .flatMapLatest { day ->
            goalDao.getGoalForDate(day.date.toEpochDays())
        }
        .map { it?.goalMl ?: DEFAULT_GOAL_ML }
        .flowOn(Dispatchers.IO)

    val drinkStats = combine(
        currentGoalAmount,
        currentDrinkAmount,
    ) { goalAmount, drinkAmount ->
        DrinkStats(
            goalAmount = goalAmount,
            drinkAmount = drinkAmount,
            goalReached = drinkAmount >= goalAmount,
        )
    }

    val goalReachedEvent = drinkStats
        .map { it.drinkAmount >= it.goalAmount }
        .distinctUntilChanged()
        .drop(1) // skip the initial state so a cold-start with goal already met does not re-fire
        .filter { it }  // only pass not-reached → reached transitions
        .map {}
}
