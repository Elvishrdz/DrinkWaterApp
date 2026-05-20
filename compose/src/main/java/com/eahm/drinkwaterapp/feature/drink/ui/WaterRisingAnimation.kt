package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class RisingBubble(
    val xFraction: Float,      // base horizontal position (0..1)
    val wobble: Float,         // horizontal wobble amplitude (fraction of width)
    val radiusFraction: Float, // radius as fraction of canvas width
    val cycleDuration: Long,   // ms for one full rise cycle
    val startOffset: Long,     // stagger offset in ms
)

data class WaterRisingConfig(
    val riseMs: Long = 1_500L,
    val holdMs: Long = 300L,
    val fadeMs: Long = 500L,
    val waveAmplitudeDp: Float = 8f,
    val waveFrequency: Float = 1.5f,
    val waveSpeed: Float = 3.5f,
    val bubbleCount: Int = 8,
    val waterColor: Color = DwWater,
    val bubbleColor: Color = Color.White.copy(alpha = 0.35f),
    val overflowDp: Float = 24f,
)

@Composable
internal fun WaterRisingAnimation(
    playing: Boolean,
    onAnimationDone: () -> Unit = {},
    modifier: Modifier = Modifier,
    config: WaterRisingConfig = WaterRisingConfig(),
) {
    var fillFraction by remember { mutableFloatStateOf(0f) }
    var animAlpha by remember { mutableFloatStateOf(0f) }
    var timeMs by remember { mutableFloatStateOf(0f) }

    val bubbles = remember(config.bubbleCount) {
        val rng = Random(42)
        List(config.bubbleCount) {
            RisingBubble(
                xFraction = 0.05f + rng.nextFloat() * 0.9f,
                wobble = 0.025f + rng.nextFloat() * 0.04f,
                radiusFraction = 0.008f + rng.nextFloat() * 0.012f,
                cycleDuration = 700L + (rng.nextFloat() * 800).toLong(),
                startOffset = (rng.nextFloat() * 1_500).toLong(),
            )
        }
    }

    val density = LocalDensity.current
    val waveAmplitudePx = with(density) { config.waveAmplitudeDp.dp.toPx() }
    val overflowPx = with(density) { config.overflowDp.dp.toPx() }

    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect

        fillFraction = 0f
        animAlpha = 1f
        timeMs = 0f

        var startMs = -1L
        val totalMs = config.riseMs + config.holdMs + config.fadeMs

        while (isActive) {
            val frameMs = withFrameMillis { it }
            if (startMs < 0L) startMs = frameMs
            val elapsed = frameMs - startMs

            timeMs = elapsed.toFloat()

            when {
                elapsed <= config.riseMs -> {
                    fillFraction = elapsed.toFloat() / config.riseMs.toFloat()
                    animAlpha = 1f
                }

                elapsed <= config.riseMs + config.holdMs -> {
                    fillFraction = 1f
                    animAlpha = 1f
                }

                elapsed <= totalMs -> {
                    fillFraction = 1f
                    val fadeElapsed = elapsed - config.riseMs - config.holdMs
                    animAlpha = 1f - fadeElapsed.toFloat() / config.fadeMs.toFloat()
                }

                else -> {
                    animAlpha = 0f
                    fillFraction = 0f
                    onAnimationDone()
                    break
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        if (animAlpha <= 0f) return@Canvas

        val waterTopY = size.height * (1f - fillFraction) - overflowPx
        val wavePhase = (timeMs / 1_000f) * config.waveSpeed

        // Draw water body with sine-wave top edge
        val path = Path()
        path.moveTo(0f, size.height)
        var px = 0f
        while (px < size.width) {
            val wy = waterTopY + waveAmplitudePx * sin(
                (px / size.width) * 2f * PI.toFloat() * config.waveFrequency + wavePhase
            )
            path.lineTo(px, wy)
            px += 4f
        }
        // Ensure the wave ends exactly at the right edge
        path.lineTo(
            size.width,
            waterTopY + waveAmplitudePx * sin(
                2f * PI.toFloat() * config.waveFrequency + wavePhase
            )
        )
        path.lineTo(size.width, size.height)
        path.close()

        drawPath(
            path = path,
            color = config.waterColor.copy(alpha = animAlpha),
            style = Fill,
        )

        // Draw bubbles rising within the water area
        val waterHeight = size.height - waterTopY
        if (waterHeight > 0f) {
            for (bubble in bubbles) {
                val t = ((timeMs.toLong() + bubble.startOffset) % bubble.cycleDuration)
                    .toFloat() / bubble.cycleDuration.toFloat()
                val bx =
                    size.width * (bubble.xFraction + bubble.wobble * sin(t * 4f * PI.toFloat()))
                val by = size.height - t * waterHeight

                if (by >= waterTopY) {
                    drawCircle(
                        color = config.bubbleColor.copy(alpha = animAlpha),
                        radius = size.width * bubble.radiusFraction,
                        center = Offset(bx.coerceIn(0f, size.width), by),
                    )
                }
            }
        }
    }
}
