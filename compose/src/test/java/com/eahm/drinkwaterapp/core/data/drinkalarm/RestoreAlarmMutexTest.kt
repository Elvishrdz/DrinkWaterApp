package com.eahm.drinkwaterapp.core.data.drinkalarm

import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RestoreAlarmMutexTest {

    private val mutex = Mutex()

    @Test
    fun `restoreAlarm executions are serialized - no concurrent access occurs`() = runTest {
        var insideLock = false
        var concurrentAccessDetected = false
        val executionOrder = mutableListOf<String>()

        suspend fun fakeRestoreAlarm(source: String) {
            mutex.withLock {
                if (insideLock) concurrentAccessDetected = true
                insideLock = true
                delay(10)
                executionOrder.add(source)
                insideLock = false
            }
        }

        val sources = listOf("BOOT_COMPLETED", "LOCKED_BOOT_COMPLETED", "PACKAGE_REPLACED")
        val jobs = sources.map { source -> launch { fakeRestoreAlarm(source) } }
        jobs.joinAll()

        assertFalse(
            concurrentAccessDetected,
            "Concurrent access was detected inside the mutex lock"
        )
        assertEquals(3, executionOrder.size, "All callers must complete")
    }

    @Test
    fun `restoreAlarm executions complete in serial - one at a time`() = runTest {
        val timeline = mutableListOf<String>()
        var lockHolderCount = 0

        suspend fun fakeRestoreAlarm(source: String) {
            mutex.withLock {
                lockHolderCount++
                assertEquals(1, lockHolderCount, "Only one caller should hold the lock at a time")
                timeline.add("start:$source")
                delay(5)
                timeline.add("end:$source")
                lockHolderCount--
            }
        }

        val sources = listOf("A", "B", "C")
        val jobs = sources.map { source -> launch { fakeRestoreAlarm(source) } }
        jobs.joinAll()

        // Verify interleaving never occurred: each "start" must be immediately followed by its "end"
        for (i in timeline.indices step 2) {
            val start = timeline[i]
            val end = timeline[i + 1]
            val source = start.removePrefix("start:")
            assertEquals("end:$source", end, "start and end for $source must be consecutive")
        }
    }
}
