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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationVariant
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.getStyle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Per-edge drag boundary for [GoalCelebrationScreen].
 *
 * - **Non-null** → that edge is constrained. The value is the inset margin from the
 *   physical edge (0.dp = flush to the edge, 10.dp = 10 dp inside the edge).
 * - **null** → no constraint on that edge; the card can be dragged off-screen freely.
 */
data class DragConstraints(
    val top: Dp? = null,
    val bottom: Dp? = null,
    val start: Dp? = null,
    val end: Dp? = null,
)

/** Pre-computed pixel clamp limits derived from [DragConstraints]. */
private data class DragLimits(
    val minX: Float = Float.NEGATIVE_INFINITY,
    val maxX: Float = Float.POSITIVE_INFINITY,
    val minY: Float = Float.NEGATIVE_INFINITY,
    val maxY: Float = Float.POSITIVE_INFINITY,
)

@Composable
fun GoalCelebrationScreen(
    variant: CelebrationVariant,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
    draggable: Boolean = false,
    dragConstraints: DragConstraints = DragConstraints(),
    showScrim: Boolean = true,
    onClose: () -> Unit,
    onShare: (Bitmap) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    var dragEnabled by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    // Natural (zero-offset) bounds of the Card composable in root coordinates.
    // Captured from inside GoalCelebrationCard, after wrapContentSize, so the size
    // is the card's actual visual size — not the full-screen wrapper that wrapContentSize
    // reports to its parent. The current dragOffset is subtracted on each update so this
    // always reflects where the card would be at dragOffset = (0, 0).
    var naturalBounds by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(Unit) {
        visible = true
        if (draggable) {
            delay(900) // card enter (460 ms) + medallion spring (~440 ms)
            dragEnabled = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Recomputed whenever naturalBounds, constraints, or screen dimensions change.
        // While naturalBounds is still Rect.Zero (pre-layout), limits are unconstrained.
        val dragLimits = remember(naturalBounds, dragConstraints, screenWidthPx, screenHeightPx) {
            if (naturalBounds == Rect.Zero) DragLimits()
            else with(density) {
                DragLimits(
                    minX = dragConstraints.start
                        ?.let { it.toPx() - naturalBounds.left }
                        ?: Float.NEGATIVE_INFINITY,
                    maxX = dragConstraints.end
                        ?.let { screenWidthPx - it.toPx() - naturalBounds.right }
                        ?: Float.POSITIVE_INFINITY,
                    minY = dragConstraints.top
                        ?.let { it.toPx() - naturalBounds.top }
                        ?: Float.NEGATIVE_INFINITY,
                    maxY = dragConstraints.bottom
                        ?.let { screenHeightPx - it.toPx() - naturalBounds.bottom }
                        ?: Float.POSITIVE_INFINITY,
                )
            }
        }
        // rememberUpdatedState lets the gesture handler always see the latest limits
        // without restarting the pointerInput coroutine.
        val currentLimits by rememberUpdatedState(dragLimits)

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
                                val limits = currentLimits
                                dragOffset = Offset(
                                    x = (dragOffset.x + dragAmount.x).coerceIn(limits.minX, limits.maxX),
                                    y = (dragOffset.y + dragAmount.y).coerceIn(limits.minY, limits.maxY),
                                )
                            }
                        } else Modifier
                    ),
                onBoundsInRoot = { actualBounds ->
                    // actualBounds includes the current dragOffset because onGloballyPositioned
                    // fires after the offset modifier places the Card. Subtract it to recover
                    // the natural position (where the card sits at dragOffset = zero).
                    naturalBounds = Rect(
                        left = actualBounds.left - dragOffset.x,
                        top = actualBounds.top - dragOffset.y,
                        right = actualBounds.right - dragOffset.x,
                        bottom = actualBounds.bottom - dragOffset.y,
                    )
                },
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
