package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingStep(
    val art: (@Composable () -> Unit)? = null,
    val title: String,
    val subtitle: String,
    val onBack: (() -> Unit)? = null,
    val footer: @Composable ColumnScope.() -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)
