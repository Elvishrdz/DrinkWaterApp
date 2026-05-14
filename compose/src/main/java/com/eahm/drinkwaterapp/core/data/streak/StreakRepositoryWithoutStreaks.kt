package com.eahm.drinkwaterapp.core.data.streak

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@OptIn(ExperimentalCoroutinesApi::class)
class StreakRepositoryWithoutStreaks(
    val database: AppDatabase,
    prefsStorage: PrefsStorage,
) : StreakRepository {

    private val historyDao = database.getHistoryDao()

    override val streakData: Flow<StreakData> = combine(
        historyDao.getHighestDrinkDay(),
        prefsStorage.getBestDrinkStreak(),
    ) { bestDay, bestDrink ->
        StreakData(
            drinkStreakDays = 0,
            bestDrinkStreakDays = 0,
            goalStreakDays = 0,
            bestGoalStreakDays = bestDrink,
            personalBestMl = bestDay?.totalDrinkAmount,
            personalBestDate = bestDay?.day,
        )
    }
}
