package com.eahm.drinkwaterapp.feature.onboarding.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import kotlinx.coroutines.delay

@Composable
fun DwPrimaryButtonAnimated(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    delayMs: Long = 1_000L,
) {
    val isPreview = LocalInspectionMode.current
    var buttonVisible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        buttonVisible = true
    }

    AnimatedVisibility(
        visible = buttonVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            initialOffsetY = { it },
        ),
    ) {
        DwPrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}