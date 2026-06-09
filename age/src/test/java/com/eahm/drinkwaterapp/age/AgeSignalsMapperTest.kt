package com.eahm.drinkwaterapp.age

import com.eahm.drinkwaterapp.age.api.AdTreatment
import com.eahm.drinkwaterapp.age.api.AgeError
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeStatus
import com.eahm.drinkwaterapp.age.internal.AgeSignalsMapper
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AgeSignalsMapperTest {

    private fun result(status: Int?, lower: Int? = null, upper: Int? = null): AgeSignalsResult =
        mockk {
            every { userStatus() } returns status
            every { ageLower() } returns lower
            every { ageUpper() } returns upper
        }

    @Test
    fun `null status is not covered`() {
        assertEquals(AgeResolution.NotCovered, AgeSignalsMapper.toResolution(result(null)))
    }

    @Test
    fun `unknown status is covered but age unknown`() {
        assertEquals(
            AgeResolution.CoveredAgeUnknown,
            AgeSignalsMapper.toResolution(result(AgeSignalsVerificationStatus.UNKNOWN)),
        )
    }

    @Test
    fun `verified adult is covered with standard treatment`() {
        val resolution = AgeSignalsMapper.toResolution(
            result(AgeSignalsVerificationStatus.VERIFIED, lower = 18, upper = null),
        )
        val covered = assertIs<AgeResolution.Covered>(resolution)
        assertEquals(AgeStatus.VERIFIED, covered.signals.status)
        assertEquals(AdTreatment.STANDARD, covered.signals.adTreatment)
    }

    @Test
    fun `supervised child is covered with child directed treatment`() {
        val resolution = AgeSignalsMapper.toResolution(
            result(AgeSignalsVerificationStatus.SUPERVISED, lower = 0, upper = 12),
        )
        val covered = assertIs<AgeResolution.Covered>(resolution)
        assertEquals(AdTreatment.CHILD_DIRECTED, covered.signals.adTreatment)
    }

    @Test
    fun `network error maps to retryable AgeError`() {
        val ex =
            mockk<AgeSignalsException> { every { errorCode } returns AgeSignalsErrorCode.NETWORK_ERROR }
        assertEquals(AgeError.NETWORK_ERROR, AgeSignalsMapper.toError(ex))
    }

    @Test
    fun `app not owned maps to non-retryable AgeError`() {
        val ex =
            mockk<AgeSignalsException> { every { errorCode } returns AgeSignalsErrorCode.APP_NOT_OWNED }
        val error = AgeSignalsMapper.toError(ex)
        assertEquals(AgeError.APP_NOT_OWNED, error)
        assertEquals(false, error.retryable)
    }

    @Test
    fun `non-age exception maps to UNKNOWN`() {
        assertEquals(AgeError.UNKNOWN, AgeSignalsMapper.toError(IllegalStateException("boom")))
    }
}
