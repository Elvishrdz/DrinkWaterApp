package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import kotlinx.coroutines.flow.combine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val activeHoursObserverModule = module {
    singleOf(::ActiveHoursObserver)
}

class ActiveHoursObserver(
    clockTickObserver: ClockTickObserver,
    prefsStorage: PrefsStorage,
) {

    val inActiveHours = combine(
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
        clockTickObserver.observe(ClockGranularity.SECOND),
    ) { startTime, endTime, currentDateTime ->
        val start = startTime.toLocalDateTime()
        val end = endTime.toLocalDateTime()

        currentDateTime in start..end
    }
}
