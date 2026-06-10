package com.eahm.drinkwaterapp.age

import com.eahm.drinkwaterapp.age.api.AgeDebugPreferences
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.eahm.drinkwaterapp.age.api.AgeSimulatedResponse
import com.eahm.drinkwaterapp.age.internal.SwitchableAgeSignalsProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class SwitchableAgeSignalsProviderTest {

    private class FakePrefs(
        var enabled: Boolean = false,
        var selected: String? = null,
    ) : AgeDebugPreferences {
        override suspend fun isOverrideEnabled(): Boolean = enabled
        override suspend fun setOverrideEnabled(enabled: Boolean) { this.enabled = enabled }
        override suspend fun getSelectedResponse(): String? = selected
        override suspend fun setSelectedResponse(name: String?) { selected = name }
    }

    private val realResolution = AgeResolution.NotCovered
    private val delegate = AgeSignalsProvider { realResolution }

    @Test
    fun `release build never honors the override`() = runTest {
        val prefs = FakePrefs(enabled = true, selected = AgeSimulatedResponse.COVERED_CHILD.name)
        val provider = SwitchableAgeSignalsProvider(delegate, prefs, isAvailable = false)

        assertSame(realResolution, provider.resolve())
        assertNull(provider.getSelectedResponse())
        assertEquals(false, provider.isOverrideEnabled())
    }

    @Test
    fun `debug build with override disabled delegates to real provider`() = runTest {
        val prefs = FakePrefs(enabled = false, selected = AgeSimulatedResponse.COVERED_CHILD.name)
        val provider = SwitchableAgeSignalsProvider(delegate, prefs, isAvailable = true)

        assertSame(realResolution, provider.resolve())
    }

    @Test
    fun `debug build with override enabled returns the pinned response`() = runTest {
        val prefs = FakePrefs(enabled = true, selected = AgeSimulatedResponse.COVERED_CHILD.name)
        val provider = SwitchableAgeSignalsProvider(delegate, prefs, isAvailable = true)

        assertEquals(AgeSimulatedResponse.COVERED_CHILD.resolution, provider.resolve())
    }

    @Test
    fun `enabled but nothing selected delegates to real provider`() = runTest {
        val prefs = FakePrefs(enabled = true, selected = null)
        val provider = SwitchableAgeSignalsProvider(delegate, prefs, isAvailable = true)

        assertSame(realResolution, provider.resolve())
    }

    @Test
    fun `setters are ignored on release builds`() = runTest {
        val prefs = FakePrefs()
        val provider = SwitchableAgeSignalsProvider(delegate, prefs, isAvailable = false)

        provider.setOverrideEnabled(true)
        provider.setSelectedResponse(AgeSimulatedResponse.NOT_COVERED)

        assertEquals(false, prefs.enabled)
        assertNull(prefs.selected)
    }
}
