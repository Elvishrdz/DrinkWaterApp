package com.eahm.drinkwaterapp.core.data.drinkalarm.alarmtime

import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.createWithSeconds
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DayTimeCreateWithSecondsTest {

    @Test
    fun `create rounded up minute value with seconds`() {
        val result = DayTime.createWithSeconds(699)

        val illegal = DayTime(0, 11)
        val expected = DayTime(0, 12)

        assertEquals(expected, result)
        assertNotEquals(illegal, result)
    }

    @Test
    fun `create fifteen in the afternoon after exceeding two days with seconds`() {
        val seconds = 2.days.plus(15.hours).inWholeSeconds.toInt()
        assertEquals(226800, seconds)

        val result = DayTime.createWithSeconds(seconds)

        val expected = DayTime(15, 0)

        assertEquals(expected, result)
    }

    @Test
    fun `create five in the morning after exceeding one day with seconds`() {
        val seconds = 1.days.plus(5.hours).inWholeSeconds.toInt()
        assertEquals(104400, seconds)

        val result = DayTime.createWithSeconds(seconds)

        val expected = DayTime(5, 0)

        assertEquals(expected, result)

    }

    @Test
    fun `create two hour with twenty minutes with seconds`() {
        val seconds = 2.hours.plus(20.minutes).inWholeSeconds.toInt()
        assertEquals(8400, seconds)

        val result = DayTime.createWithSeconds(seconds)

        val expected = DayTime(2, 20)

        assertEquals(expected, result)
    }

    @Test
    fun `create one hour with seconds`() {
        val seconds = 1.hours.inWholeSeconds.toInt()
        assertEquals(3600, seconds)

        val result = DayTime.createWithSeconds(seconds)

        val expected = DayTime(1, 0)

        assertEquals(expected, result)
    }

    @Test
    fun `create one minute with seconds`() {
        val seconds = 1.minutes.inWholeSeconds.toInt()
        assertEquals(60, seconds)

        val result = DayTime.createWithSeconds(seconds)

        val expected = DayTime(0, 1)

        assertEquals(expected, result)
    }
}