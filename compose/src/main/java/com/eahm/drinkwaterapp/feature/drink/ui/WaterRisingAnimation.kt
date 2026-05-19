package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Bubble(
    val startX: Float,   // fraction of width  (0..1)
    val startY: Float,   // fraction of height (0..1) — starting below center-ish
    val radius: Float,   // fraction of width
    val speedMult: Float, // relative speed (0.5..1.5)
    val wobble: Float,   // horizontal wobble amplitude (fraction of width)
    val color: Color,
)

@Composable
internal fun WaterRisingAnimation(
    trigger: Int,         // increment to start a new animation
    modifier: Modifier = Modifier,
) {
    val bubbles = remember {
        val rng = Random(42)
        List(6) {
            Bubble(
                startX = 0.2f + rng.nextFloat() * 0.6f,
                startY = 0.6f + rng.nextFloat() * 0.25f,
                radius = 0.018f + rng.nextFloat() * 0.022f,
                speedMult = 0.6f + rng.nextFloat() * 0.9f,
                wobble = 0.03f + rng.nextFloat() * 0.04f,
                color = if (rng.nextBoolean()) DwWater else DwWaterLight,
            )
        }
    }

    // Per-bubble progress animatables (0 → 1 = bubble traveled from start to top)
    val progresses = remember(trigger) { bubbles.map { Animatable(0f) } }
    val alphas = remember(trigger) { bubbles.map { Animatable(0f) } }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        bubbles.forEachIndexed { idx, bubble ->
            launch {
                delay((idx * 80L))
                val duration = (900f / bubble.speedMult).toInt()
                alphas[idx].snapTo(0.85f)
                progresses[idx].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing),
                )
                alphas[idx].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200),
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        bubbles.forEachIndexed { idx, bubble ->
            val progress = progresses[idx].value
            val alpha = alphas[idx].value
            if (alpha <= 0f) return@forEachIndexed

            val cx = size.width * (bubble.startX + bubble.wobble * sin(progress * 6.28f * 2))
            val cy = size.height * (bubble.startY - progress * bubble.startY * 0.9f)
            val r = size.width * bubble.radius

            drawCircle(
                color = bubble.color.copy(alpha = alpha * 0.75f),
                radius = r,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
            )
            // Ring outline for larger bubbles
            if (r > size.width * 0.025f) {
                drawCircle(
                    color = bubble.color.copy(alpha = alpha * 0.4f),
                    radius = r,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.3f),
                )
            }
        }
    }
}
