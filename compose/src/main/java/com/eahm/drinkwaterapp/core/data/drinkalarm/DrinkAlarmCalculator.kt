package com.eahm.drinkwaterapp.core.data.drinkalarm

import androidx.compose.ui.util.fastRoundToInt

class DrinkAlarmCalculator {

    fun calculateDrinkInterval(
        startTime: DayTime,
        endTime: DayTime,
        goalAmountMl: Int,
        drinkIntervalMl: Float,
    ): DayTime {
        val timeDifference = DayTime.calculateTimeDifference(startTime, endTime)
        val timeDifferenceInWholeSeconds = timeDifference.inWholeSeconds

        val totalTimesToDrinkRounded = calculateDrinkCount(
            goalAmountMl = goalAmountMl,
            drinkIntervalMl = drinkIntervalMl
        )

        val drinkIntervalInSeconds =
            timeDifferenceInWholeSeconds.toFloat() / totalTimesToDrinkRounded.toFloat()

        val drinkIntervalInSecondsRounded = drinkIntervalInSeconds.fastRoundToInt()

        return DayTime.createWithSeconds(drinkIntervalInSecondsRounded)
    }

    fun calculateDrinkCount(
        goalAmountMl: Int,
        drinkIntervalMl: Float,
    ): Int {
        val totalTimesToDrink = (goalAmountMl / drinkIntervalMl)
        return totalTimesToDrink.fastRoundToInt()
    }
}
