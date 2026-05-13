package com.eahm.drinkwaterapp.core.data.goalreachedwidget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

/**
 * Full-screen overlay that centers [GoalCelebrationCard] over an animated scrim.
 *
 * Note: `backdrop-filter: blur` has no equivalent below API 31. The scrim uses
 * alpha-only (`0x6B` ≈ 42%) for broad compatibility.
 */
@Composable
fun GoalCelebrationOverlay(
    variant: CelebrationVariant,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
    showScrim: Boolean = true,
    onClose: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showScrim) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(durationMillis = 220)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x6B0E1A20))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose,
                        )
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.82f,
                animationSpec = tween(durationMillis = 460),
            ) + slideInVertically(
                initialOffsetY = { 24 },
                animationSpec = tween(durationMillis = 460),
            ),
            modifier = Modifier.align(Alignment.Center),
        ) {
            GoalCelebrationCard(
                variant = variant,
                goalMl = goalMl,
                drinkCount = drinkCount,
                streakDays = streakDays,
                onClose = onClose,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun GoalCelebrationOverlayPreview() {
    DrinkWaterAppTheme {
        GoalCelebrationOverlay(
            variant = getStyle(value = CelebrationStyle.Ocean),
            goalMl = 10f,
            drinkCount = 5,
            streakDays = 1,
            onClose = {},
        )
    }
}
