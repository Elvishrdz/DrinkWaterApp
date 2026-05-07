package com.eahm.drinkwaterapp.core.data.drinkalarm.simulator

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SimulateNextIntervalCalculationTest {

    private val startTime = createSimulatedDateTime(7, 0)

    private val endTime = createSimulatedDateTime(19, 0)


    @Test
    fun `verify time in range returns option B`() {
        val now = createSimulatedDateTime(8, 0)

        val result = simulateNextIntervalCalculation(
            now = now,
            startTime = startTime,
            endTime = endTime,
        )

        val expected = getSimulatedOptionB(now)

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `verify time equals end time returns start time`() {
        val result = simulateNextIntervalCalculation(
            now = endTime,
            startTime = startTime,
            endTime = endTime,
        )

        assertEquals(expected = startTime, actual = result)
    }

    @Test
    fun `verify time equals option C returns end time`() {
        val now = createSimulatedDateTime(7, 0)
        val optionC = getSimulatedOptionC(now)

        val result = simulateNextIntervalCalculation(
            now = optionC,
            startTime = startTime,
            endTime = endTime,
        )

        assertEquals(expected = endTime, actual = result)
    }

    @Test
    fun `verify time equals option B returns next interval C`() {
        val now = createSimulatedDateTime(7, 0)
        val optionB = getSimulatedOptionB(now)

        val result = simulateNextIntervalCalculation(
            now = optionB,
            startTime = startTime,
            endTime = endTime,
        )

        val simulatedOptionC = getSimulatedOptionC(now)

        assertEquals(expected = simulatedOptionC, actual = result)
    }

    @Test
    fun `verify time equals start time returns next interval B`() {
        val now = createSimulatedDateTime(7, 0)

        val result = simulateNextIntervalCalculation(
            now = now,
            startTime = startTime,
            endTime = endTime,
        )

        val simulatedOptionB = getSimulatedOptionB(now)

        assertEquals(expected = simulatedOptionB, actual = result)
    }

    @Test
    fun `verify time before start time returns start time`() {
        val timeZone = TimeZone.currentSystemDefault()

        val beforeStartTime = startTime.toInstant(timeZone)
            .minus(1.minutes)
            .toLocalDateTime(timeZone)

        val result = simulateNextIntervalCalculation(
            now = beforeStartTime,
            startTime = startTime,
            endTime = endTime,
        )

        assertEquals(expected = startTime, actual = result)
    }
}
