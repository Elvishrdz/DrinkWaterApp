package com.eahm.drinkwaterapp.age

import com.eahm.drinkwaterapp.age.api.AdTreatment
import com.eahm.drinkwaterapp.age.api.AgeStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdTreatmentTest {

    @Test
    fun `under 13 upper bound maps to child directed`() {
        assertEquals(AdTreatment.CHILD_DIRECTED, AdTreatment.fromSignal(AgeStatus.DECLARED, 0, 12))
    }

    @Test
    fun `teen range maps to under age of consent`() {
        assertEquals(
            AdTreatment.UNDER_AGE_OF_CONSENT,
            AdTreatment.fromSignal(AgeStatus.DECLARED, 13, 17),
        )
    }

    @Test
    fun `adult lower bound maps to standard`() {
        assertEquals(AdTreatment.STANDARD, AdTreatment.fromSignal(AgeStatus.VERIFIED, 18, null))
    }

    @Test
    fun `supervised with no bounds is treated as under age of consent`() {
        assertEquals(
            AdTreatment.UNDER_AGE_OF_CONSENT,
            AdTreatment.fromSignal(AgeStatus.SUPERVISED, null, null),
        )
    }

    @Test
    fun `verified adult with no bounds is standard`() {
        assertEquals(AdTreatment.STANDARD, AdTreatment.fromSignal(AgeStatus.VERIFIED, null, null))
    }

    @Test
    fun `fromAge derives correct buckets`() {
        assertEquals(AdTreatment.CHILD_DIRECTED, AdTreatment.fromAge(8))
        assertEquals(AdTreatment.UNDER_AGE_OF_CONSENT, AdTreatment.fromAge(15))
        assertEquals(AdTreatment.STANDARD, AdTreatment.fromAge(18))
        assertEquals(AdTreatment.STANDARD, AdTreatment.fromAge(40))
    }

    @Test
    fun `isMinor reflects bracket`() {
        assertTrue(AdTreatment.CHILD_DIRECTED.isMinor)
        assertTrue(AdTreatment.UNDER_AGE_OF_CONSENT.isMinor)
        assertFalse(AdTreatment.STANDARD.isMinor)
    }
}
