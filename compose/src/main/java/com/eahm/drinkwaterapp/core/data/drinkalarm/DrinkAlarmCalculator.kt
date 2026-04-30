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

    fun getDrinkTimes(
        startTime: DayTime,
        endTime: DayTime,
        goalAmountMl: Int,
        drinkIntervalMl: Float,
    ): List<DrinkTime> {
        val count = calculateDrinkCount(goalAmountMl, drinkIntervalMl)
        val interval = calculateDrinkInterval(startTime, endTime, goalAmountMl, drinkIntervalMl)

        val drinkTimes = mutableListOf<DrinkTime>()

        var helperInWholeSeconds = startTime.inWholeSeconds

        // TODO: check if interval is short to reach endTime by more or less the interval,
        // if yes, just set the end time as last value in the list.
        endLoop@ for (x in 0 until count) {
            val drinkTime = DrinkTime(
                time = DayTime.createWithSeconds(helperInWholeSeconds),
            )

            drinkTimes.add(drinkTime)

            helperInWholeSeconds += interval.inWholeSeconds

            if (helperInWholeSeconds > endTime.inWholeSeconds) {
                break@endLoop
            }
        }

        return drinkTimes
    }

    private fun calculateDrinkCount(
        goalAmountMl: Int,
        drinkIntervalMl: Float,
    ): Int {
        val totalTimesToDrink = (goalAmountMl / drinkIntervalMl)
        return totalTimesToDrink.fastRoundToInt()
    }
}

data class DrinkTime(
    val time: DayTime,
)
