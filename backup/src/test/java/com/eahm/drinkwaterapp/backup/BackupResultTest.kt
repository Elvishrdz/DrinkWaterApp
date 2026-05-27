package com.eahm.drinkwaterapp.backup

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackupResultTest {

    @Test
    fun `Success isSuccess is true`() {
        assertTrue(BackupResult.Success(42).isSuccess)
    }

    @Test
    fun `Success isFailure is false`() {
        assertFalse(BackupResult.Success(42).isFailure)
    }

    @Test
    fun `Failure isFailure is true`() {
        assertTrue(BackupResult.Failure(BackupError.NotSignedIn).isFailure)
    }

    @Test
    fun `Failure isSuccess is false`() {
        assertFalse(BackupResult.Failure(BackupError.NotSignedIn).isSuccess)
    }

    @Test
    fun `map transforms success value`() {
        val result = BackupResult.Success(10).map { it * 2 }
        assertEquals(BackupResult.Success(20), result)
    }

    @Test
    fun `map does not execute on failure`() {
        var called = false
        val error = BackupError.NotSignedIn
        val result = BackupResult.Failure(error).map<Int> { called = true; error("unreachable") }
        assertFalse(called)
        assertEquals(BackupResult.Failure(error), result)
    }

    @Test
    fun `getOrElse returns value on success`() {
        assertEquals(5, BackupResult.Success(5).getOrElse { -1 })
    }

    @Test
    fun `getOrElse returns default on failure`() {
        val failure: BackupResult<Int> = BackupResult.Failure(BackupError.NotSignedIn)
        assertEquals(-1, failure.getOrElse { -1 })
    }

    @Test
    fun `onSuccess callback invoked for Success`() {
        var received: Int? = null
        BackupResult.Success(99).onSuccess { received = it }
        assertEquals(99, received)
    }

    @Test
    fun `onSuccess callback not invoked for Failure`() {
        var called = false
        BackupResult.Failure(BackupError.NotSignedIn).onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onFailure callback invoked for Failure`() {
        var received: BackupError? = null
        BackupResult.Failure(BackupError.NotSignedIn).onFailure { received = it }
        assertEquals(BackupError.NotSignedIn, received)
    }

    @Test
    fun `onFailure callback not invoked for Success`() {
        var called = false
        BackupResult.Success(1).onFailure { called = true }
        assertFalse(called)
    }
}
