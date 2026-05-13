package com.eahm.drinkwaterapp.system.service.goalcelebration.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationVariant
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.getStyle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GoalCelebrationScreen(
    variant: CelebrationVariant,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
    draggable: Boolean = false,
    showScrim: Boolean = true,
    onClose: () -> Unit,
    onShare: (Bitmap) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    var dragEnabled by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        visible = true
        if (draggable) {
            delay(900) // card enter (460 ms) + medallion spring (~440 ms)
            dragEnabled = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showScrim) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(durationMillis = 220)),
                modifier = Modifier
                    .fillMaxSize(),
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
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
        ) {
            GoalCelebrationCard(
                variant = variant,
                goalMl = goalMl,
                drinkCount = drinkCount,
                streakDays = streakDays,
                onClose = onClose,
                onShare = onShare,
                showStreakDays = false, // Streak disabled until is working
                showShareProgressButton = false, // Share disabled until is fully ready
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                    .then(
                        if (dragEnabled) Modifier.pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                dragOffset += dragAmount
                            }
                        } else Modifier
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun GoalCelebrationScreenPreview() {
    DrinkWaterAppTheme {
        GoalCelebrationScreen(
            variant = getStyle(value = CelebrationStyle.Ocean),
            goalMl = 10f,
            drinkCount = 5,
            streakDays = 1,
            onClose = {},
            onShare = {},
        )
    }
}
