package com.eahm.drinkwaterapp.feature.age

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.age.api.AgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backs the one-time bootstrap age-collection screen. Persists a user-declared birth year via
 * [AgeRepository], which derives the ad-treatment bracket. The age is used only for age-appropriate
 * ads and compliance — never analytics, profiling, or targeting.
 */
internal class AgeCollectionViewModel(
    private val ageRepository: AgeRepository,
) : ViewModel() {

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    fun submitBirthYear(birthYear: Int) {
        viewModelScope.launch {
            ageRepository.submitDeclaredBirthYear(birthYear)
            _completed.value = true
        }
    }
}
