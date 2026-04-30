package com.eahm.drinkwaterapp.core.data.drinkalarm.alarmtime

import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.calculateTimeDifference
import org.junit.Test
import kotlin.test.assertEquals

class DayTimeCalculateTimeDifferenceTest {

    @Test
    fun `calculate time difference pass midnight by minutes`() {
        val result = DayTime.calculateTimeDifference(
            start = DayTime(23, 59),
            end = DayTime(0, 0),
        )

        val expected = DayTime(0, 1)

        assertEquals(expected, result)
    }

    @Test
    fun `calculate time difference pass midnight`() {
        val result = DayTime.calculateTimeDifference(
            start = DayTime(23, 0),
            end = DayTime(1, 0),
        )

        val expected = DayTime(2, 0)

        assertEquals(expected, result)
    }

    @Test
    fun `calculate time difference with hour and minutes`() {
        val result = DayTime.calculateTimeDifference(
            start = DayTime(6, 30),
            end = DayTime(19, 30),
        )

        val expected = DayTime(13, 0)

        assertEquals(expected, result)
    }

    @Test
    fun `calculate time difference with only hours`() {
        val result = DayTime.calculateTimeDifference(
            start = DayTime(6, 0),
            end = DayTime(19, 0),
        )

        val expected = DayTime(13, 0)

        assertEquals(expected, result)
    }
}
