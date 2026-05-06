package com.eahm.drinkwaterapp.core.data.drinkstatsobserver

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.feature.drink.DateTimeObserver
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
    dateTimeObserver: DateTimeObserver,
    prefsStorage: PrefsStorage,
) {

    private val dao = database.getHistoryDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentDrinkAmount = dateTimeObserver.currentDateTime
        .flatMapLatest { day ->
            dao.getTotalDrinkAmount(start = day.toStartOfDayTime, end = day.toEndOfDayTime)
        }
        .flowOn(Dispatchers.IO)

    val drinkStats = combine(
        prefsStorage.getGoalAmount(),
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
