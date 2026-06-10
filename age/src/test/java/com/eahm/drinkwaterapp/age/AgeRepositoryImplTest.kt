package com.eahm.drinkwaterapp.age

import com.eahm.drinkwaterapp.age.api.AdTreatment
import com.eahm.drinkwaterapp.age.api.AgeError
import com.eahm.drinkwaterapp.age.api.AgePreferences
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.eahm.drinkwaterapp.age.api.AgeSource
import com.eahm.drinkwaterapp.age.api.AgeStatus
import com.eahm.drinkwaterapp.age.internal.AgeRepositoryImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgeRepositoryImplTest {

    private class FakePreferences(var stored: AgeSignals? = null) : AgePreferences {
        override suspend fun getAgeSignals(): AgeSignals? = stored
        override suspend fun setAgeSignals(signals: AgeSignals) {
            stored = signals
        }

        override suspend fun clearAgeSignals() {
            stored = null
        }
    }

    private fun repo(
        resolution: AgeResolution,
        prefs: FakePreferences = FakePreferences(),
        year: Int = 2026,
    ) = AgeRepositoryImpl(
        provider = AgeSignalsProvider { resolution },
        preferences = prefs,
        currentYear = { year },
    )

    @Test
    fun `covered API result is persisted and never prompts`() = runTest {
        val prefs = FakePreferences()
        val signals = AgeSignals.fromApi(AgeStatus.VERIFIED, 18, null)
        val repo = repo(AgeResolution.Covered(signals), prefs)

        repo.refresh()

        assertEquals(signals, repo.ageSignals.value)
        assertEquals(signals, prefs.stored)
        assertFalse(repo.needsAgeCollection.value)
    }

    @Test
    fun `not covered never prompts and stores nothing`() = runTest {
        val prefs = FakePreferences()
        val repo = repo(AgeResolution.NotCovered, prefs)

        repo.refresh()

        assertFalse(repo.needsAgeCollection.value)
        assertNull(prefs.stored)
        assertNull(repo.ageSignals.value)
    }

    @Test
    fun `covered age unknown with no stored age prompts`() = runTest {
        val repo = repo(AgeResolution.CoveredAgeUnknown)
        repo.refresh()
        assertTrue(repo.needsAgeCollection.value)
    }

    @Test
    fun `undetermined with no stored age prompts (first-run bootstrap)`() = runTest {
        val repo = repo(AgeResolution.Undetermined(AgeError.APP_NOT_OWNED))
        repo.refresh()
        assertTrue(repo.needsAgeCollection.value)
    }

    @Test
    fun `undetermined with stored age does not prompt`() = runTest {
        val prefs = FakePreferences(AgeSignals.fromDeclaredAge(30))
        val repo = repo(AgeResolution.Undetermined(AgeError.NETWORK_ERROR), prefs)

        repo.refresh()

        assertFalse(repo.needsAgeCollection.value)
        assertEquals(AdTreatment.STANDARD, repo.ageSignals.value?.adTreatment)
    }

    @Test
    fun `definitive API result supersedes a previously declared value`() = runTest {
        val prefs = FakePreferences(AgeSignals.fromDeclaredAge(30))
        val apiSignals = AgeSignals.fromApi(AgeStatus.SUPERVISED, 0, 12)
        val repo = repo(AgeResolution.Covered(apiSignals), prefs)

        repo.refresh()

        assertEquals(AgeSource.API, repo.ageSignals.value?.source)
        assertEquals(AdTreatment.CHILD_DIRECTED, repo.ageSignals.value?.adTreatment)
        assertEquals(apiSignals, prefs.stored)
    }

    @Test
    fun `submitDeclaredBirthYear stores derived bracket and clears prompt`() = runTest {
        val prefs = FakePreferences()
        val repo = repo(AgeResolution.CoveredAgeUnknown, prefs, year = 2026)
        repo.refresh()
        assertTrue(repo.needsAgeCollection.value)

        repo.submitDeclaredBirthYear(2015) // age 11

        assertFalse(repo.needsAgeCollection.value)
        assertEquals(AdTreatment.CHILD_DIRECTED, repo.ageSignals.value?.adTreatment)
        assertEquals(AgeSource.DECLARED, prefs.stored?.source)
    }

    @Test
    fun `submitDeclaredBracket stores the chosen bracket and clears prompt`() = runTest {
        val prefs = FakePreferences()
        val repo = repo(AgeResolution.CoveredAgeUnknown, prefs)
        repo.refresh()
        assertTrue(repo.needsAgeCollection.value)

        repo.submitDeclaredBracket(AdTreatment.UNDER_AGE_OF_CONSENT)

        assertFalse(repo.needsAgeCollection.value)
        assertEquals(AdTreatment.UNDER_AGE_OF_CONSENT, repo.ageSignals.value?.adTreatment)
        assertEquals(AgeSource.DECLARED, prefs.stored?.source)
        assertEquals(13, prefs.stored?.ageLower)
        assertEquals(17, prefs.stored?.ageUpper)
    }
}
