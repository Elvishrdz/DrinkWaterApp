package com.eahm.drinkwaterapp.core.data.streak

import com.eahm.drinkwaterapp.core.config.FeatureFlags
import kotlinx.coroutines.flow.Flow
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val streakModule = module {
    if (FeatureFlags.streakFeatureEnabled) {
        singleOf(::StreakRepositoryImpl) bind StreakRepository::class
    } else {
        singleOf(::StreakRepositoryWithoutStreaks) bind StreakRepository::class
    }
}

interface StreakRepository {

    val streakData: Flow<StreakData>
}
