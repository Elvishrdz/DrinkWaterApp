package com.eahm.drinkwaterapp.core.data.drinkalarm

import org.junit.Test
import kotlin.test.assertEquals

class DrinkAlarmCalculatorTest {

    val calculator = DrinkAlarmCalculator()

    @Test
    fun `determine drink interval with start and end times`() {
        val startTime = DayTime(6, 0)
        val endTime = DayTime(19, 0)
        val goalAmountMl = 2_000
        val drinkIntervalMl = 30f

        val result = calculator.calculateDrinkInterval(
            startTime = startTime,
            endTime = endTime,
            goalAmountMl = goalAmountMl,
            drinkIntervalMl = drinkIntervalMl,
        )

        val expected = DayTime(0, 12)

        assertEquals(expected, result)
    }
}
