package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Scales the composable down on tap and springs back with a bounce on release.
 *
 * @param onClick       Click callback.
 * @param shape         Shape used to clip the touch area (and the ripple if enabled).
 * @param enabled       Set to false to disable the scale animation (click still fires).
 * @param rippleEnabled Set to true to show the platform ripple on tap.
 */
fun Modifier.bounceTap(
    onClick: () -> Unit,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    rippleEnabled: Boolean = false,
): Modifier = composed {
    val bounceEvent = remember { RingAnimationEvent() }
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    val currentEnabled = rememberUpdatedState(enabled)

    LaunchedEffect(bounceEvent) {
        bounceEvent.interactions.collect {
            if (!currentEnabled.value) return@collect
            // Fast compress, then bouncy release
            scale.animateTo(0.93f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    this
        .scale(scale.value)
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = if (rippleEnabled) LocalIndication.current else null,
            onClick = {
                bounceEvent.emitBounce()
                onClick()
            },
        )
}

class RingAnimationEvent {

    val interactions = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun emitBounce() {
        interactions.tryEmit(Unit)
    }
}

/**
 * Draws a sweeping shimmer arc around the ring at a fixed interval, simulating an idle reflection.
 * The shimmer fades in/out and is rendered as three overlapping arcs (soft glow → mid → bright core)
 * to produce a gradient-like highlight that reads as a passing reflection.
 *
 * Intended for use when the ring is at 0% progress.
 *
 * @param enabled      Set to false to skip the animation entirely.
 * @param strokeWidth  Must match the ring's stroke width so the shimmer aligns with the arc track.
 * @param intervalMs   Milliseconds between each shimmer pass.
 * @param color        Shimmer highlight colour.
 */
fun Modifier.idleShimmer(
    enabled: Boolean = true,
    strokeWidth: Dp = 14.dp,
    intervalMs: Long = 1200L,
    color: Color = DwWaterLight.copy(alpha = 0.2f),
): Modifier = if (!enabled) this else composed {
    val sweepDurationMs = 900
    val angle = remember { Animatable(-90f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMs)
            angle.snapTo(-90f)
            alpha.snapTo(0f)
            // Fade in → hold → fade out in sync with the sweep
            launch {
                alpha.animateTo(1f, tween(sweepDurationMs / 4))
                delay((sweepDurationMs / 2).toLong())
                alpha.animateTo(0f, tween(sweepDurationMs / 4))
            }
            angle.animateTo(
                targetValue = 270f,
                animationSpec = tween(sweepDurationMs, easing = LinearEasing),
            )
        }
    }

    drawWithContent {
        drawContent()
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcTopLeft = Offset(inset, inset)
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        val peak = alpha.value

        // Three overlapping arcs in painter's model (each layer on top of the previous):
        // dim outer glow → medium transition → bright core
        // Result: soft dim entry → bright peak → soft dim exit
        drawArc(
            color = color.copy(alpha = peak * 0.28f),
            startAngle = angle.value,
            sweepAngle = 44f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        drawArc(
            color = color.copy(alpha = peak * 0.60f),
            startAngle = angle.value + 11f,
            sweepAngle = 22f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
        )
        drawArc(
            color = color.copy(alpha = peak * 0.95f),
            startAngle = angle.value + 17f,
            sweepAngle = 10f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Butt),
        )
    }
}
