package com.eahm.drinkwaterapp.core.data.streak

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class StreakRepositoryMock(
    prefsStorage: PrefsStorage,
) : StreakRepository {

    override val streakData: Flow<StreakData> = prefsStorage.getBestDrinkStreak().map { bestDrink ->
        StreakData(
            drinkStreakDays = 0,
            goalStreakDays = 0,
            bestDrinkStreakDays = 0,
            bestGoalStreakDays = bestDrink,
        )
    }
}
