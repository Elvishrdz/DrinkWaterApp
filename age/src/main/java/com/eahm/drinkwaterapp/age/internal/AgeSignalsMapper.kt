package com.eahm.drinkwaterapp.age.internal

import com.eahm.drinkwaterapp.age.api.AgeError
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeStatus
import com.google.android.play.agesignals.AgeSignalsException
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.android.play.agesignals.model.AgeSignalsErrorCode
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus

/** Maps the Play SDK's primitive results/errors into our Play-free [AgeResolution] / [AgeError]. */
internal object AgeSignalsMapper {

    /**
     * Maps a successful API result. `userStatus == null` means the user is not in a covered region;
     * `UNKNOWN` means covered but age unknown; anything else is a usable signal.
     */
    fun toResolution(result: AgeSignalsResult): AgeResolution {
        val status: Int = result.userStatus() ?: return AgeResolution.NotCovered
        if (status == AgeSignalsVerificationStatus.UNKNOWN) return AgeResolution.CoveredAgeUnknown

        val ageStatus = status.toAgeStatus() ?: return AgeResolution.CoveredAgeUnknown
        return AgeResolution.Covered(
            AgeSignals.fromApi(
                status = ageStatus,
                ageLower = result.ageLower(),
                ageUpper = result.ageUpper(),
            ),
        )
    }

    /** Maps a failed [com.google.android.gms.tasks.Task] exception into a typed [AgeError]. */
    fun toError(throwable: Throwable): AgeError {
        val code = (throwable as? AgeSignalsException)?.errorCode ?: return AgeError.UNKNOWN
        return when (code) {
            AgeSignalsErrorCode.API_NOT_AVAILABLE -> AgeError.API_NOT_AVAILABLE
            AgeSignalsErrorCode.NETWORK_ERROR -> AgeError.NETWORK_ERROR
            AgeSignalsErrorCode.PLAY_STORE_NOT_FOUND,
            AgeSignalsErrorCode.PLAY_SERVICES_NOT_FOUND,
            AgeSignalsErrorCode.CANNOT_BIND_TO_SERVICE,
            AgeSignalsErrorCode.PLAY_STORE_VERSION_OUTDATED,
            AgeSignalsErrorCode.PLAY_SERVICES_VERSION_OUTDATED,
                -> AgeError.PLAY_UNAVAILABLE

            AgeSignalsErrorCode.CLIENT_TRANSIENT_ERROR,
            AgeSignalsErrorCode.INTERNAL_ERROR,
                -> AgeError.TRANSIENT

            AgeSignalsErrorCode.APP_NOT_OWNED -> AgeError.APP_NOT_OWNED
            AgeSignalsErrorCode.SDK_VERSION_OUTDATED -> AgeError.SDK_VERSION_OUTDATED
            else -> AgeError.UNKNOWN
        }
    }

    private fun Int.toAgeStatus(): AgeStatus? = when (this) {
        AgeSignalsVerificationStatus.VERIFIED -> AgeStatus.VERIFIED
        AgeSignalsVerificationStatus.DECLARED -> AgeStatus.DECLARED
        AgeSignalsVerificationStatus.SUPERVISED -> AgeStatus.SUPERVISED
        AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_PENDING -> AgeStatus.SUPERVISED_APPROVAL_PENDING
        AgeSignalsVerificationStatus.SUPERVISED_APPROVAL_DENIED -> AgeStatus.SUPERVISED_APPROVAL_DENIED
        else -> null
    }
}
