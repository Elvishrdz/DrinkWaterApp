package com.eahm.drinkwaterapp.age.internal

import android.content.Context
import com.eahm.drinkwaterapp.age.api.AgeError
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsManagerFactory
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds

/**
 * [AgeSignalsProvider] backed by the Play Age Signals API.
 * Retries retryable errors with exponential backoff before giving up with
 * [AgeResolution.Undetermined].
 */
internal class PlayAgeSignalsProvider(
    private val managerProvider: () -> AgeSignalsManager,
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    private val initialBackoffMillis: Long = DEFAULT_INITIAL_BACKOFF_MILLIS,
) : AgeSignalsProvider {

    constructor(context: Context) : this(
        managerProvider = { AgeSignalsManagerFactory.create(context.applicationContext) },
    )

    @Suppress("TooGenericExceptionCaught")
    override suspend fun resolve(): AgeResolution {
        var backoff = initialBackoffMillis
        repeat(maxAttempts) { attempt ->
            try {
                return AgeSignalsMapper.toResolution(checkOnce())
            } catch (throwable: Throwable) {
                val error = AgeSignalsMapper.toError(throwable)
                val lastAttempt = attempt == maxAttempts - 1
                if (!error.retryable || lastAttempt) {
                    return AgeResolution.Undetermined(error)
                }
                delay(backoff.milliseconds)
                backoff *= 2
            }
        }
        // Unreachable: the loop always returns, but the compiler needs a terminal expression.
        return AgeResolution.Undetermined(AgeError.UNKNOWN)
    }

    private suspend fun checkOnce(): AgeSignalsResult =
        suspendCancellableCoroutine { continuation ->
            managerProvider()
                .checkAgeSignals(AgeSignalsRequest.builder().build())
                .addOnSuccessListener { result -> continuation.resume(result) }
                .addOnFailureListener { throwable -> continuation.resumeWithException(throwable) }
        }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val DEFAULT_INITIAL_BACKOFF_MILLIS = 500L
    }
}
