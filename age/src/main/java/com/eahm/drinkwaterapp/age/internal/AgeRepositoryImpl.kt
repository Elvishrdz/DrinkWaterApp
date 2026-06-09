package com.eahm.drinkwaterapp.age.internal

import com.eahm.drinkwaterapp.age.api.AgePreferences
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

internal class AgeRepositoryImpl(
    private val provider: AgeSignalsProvider,
    private val preferences: AgePreferences,
    private val currentYear: () -> Int = { Calendar.getInstance().get(Calendar.YEAR) },
) : AgeRepository {

    private val _ageSignals = MutableStateFlow<AgeSignals?>(null)
    override val ageSignals: StateFlow<AgeSignals?> = _ageSignals.asStateFlow()

    private val _needsAgeCollection = MutableStateFlow(false)
    override val needsAgeCollection: StateFlow<Boolean> = _needsAgeCollection.asStateFlow()

    @Suppress("Indentation")
    override suspend fun refresh() {
        // Apply any cached value immediately so ads get a treatment before the async call returns.
        val stored = preferences.getAgeSignals()
        if (stored != null) _ageSignals.value = stored

        when (val resolution = provider.resolve()) {
            is AgeResolution.Covered -> {
                // The API is authoritative and supersedes any declared value.
                persist(resolution.signals)
                _needsAgeCollection.value = false
            }

            // Definitive "not in a covered region": no screen, standard ads.
            AgeResolution.NotCovered -> _needsAgeCollection.value = false

            // No usable signal. Prompt only as a one-time bootstrap: first run, nothing stored.
            AgeResolution.CoveredAgeUnknown,
            is AgeResolution.Undetermined,
                -> _needsAgeCollection.value = stored == null
        }
    }

    override suspend fun submitDeclaredBirthYear(birthYear: Int) {
        val age = (currentYear() - birthYear).coerceAtLeast(0)
        persist(AgeSignals.fromDeclaredAge(age))
        _needsAgeCollection.value = false
    }

    private suspend fun persist(signals: AgeSignals) {
        preferences.setAgeSignals(signals)
        _ageSignals.value = signals
    }
}
