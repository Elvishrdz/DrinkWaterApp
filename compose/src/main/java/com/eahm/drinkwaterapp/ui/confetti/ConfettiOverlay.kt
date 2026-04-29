package com.eahm.drinkwaterapp.ui.confetti

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Controls how confetti pieces fade as they fall.
 *
 * [NONE]        — pieces travel the full screen height and disappear off the bottom edge.
 * [BOTTOM_HALF] — pieces start fading at 50 % of the screen height and are fully
 *                 transparent by 80 %, giving a "dissolve mid-air" feel.
 */
enum class ConfettiFadeMode { NONE, BOTTOM_HALF }

val confettiDefaultColors: List<Color> = listOf(
    Color(0xFFA22C83),
    Color(0xFF1C8EBF),
    Color(0xFF4FB3DD),
    Color(0xFF00A051),
    Color(0xFFF4A622),
    Color(0xFFFF6B9D),
    Color(0xFF845EC2),
    Color(0xFFFF9671),
    Color(0xFF00C9A7),
    Color(0xFFFFC75F),
)

private enum class ConfettiShape { RECT, CIRCLE }

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,       // negative pixels → starts above the screen
    val velY: Float,         // fall speed  px / ms
    val drift: Float,        // steady horizontal drift  px / ms
    val color: Color,
    val sizePx: Float,       // bounding-box size in pixels
    val shape: ConfettiShape,
    val aspectRatio: Float,  // width / height (RECT only; 1f for CIRCLE)
    val startRotation: Float,
    val rotSpeed: Float,     // degrees / ms
    val wobbleFreq: Float,   // radians / ms
    val wobbleAmp: Float,    // horizontal wobble amplitude  px
    val phase: Float,        // wobble sine phase offset
    val delay: Float,        // ms before this particle begins moving (stagger)
) {
    private fun et(t: Float) = (t - delay).coerceAtLeast(0f)

    fun xAt(t: Float): Float = startX + drift * et(t) + wobbleAmp * sin(wobbleFreq * et(t) + phase)
    fun yAt(t: Float): Float = startY + velY * et(t)
    fun rotAt(t: Float): Float = startRotation + rotSpeed * et(t)
    fun isFinished(t: Float, screenH: Float): Boolean = yAt(t) > screenH + sizePx
}

private fun createParticle(
    screenW: Float,
    screenH: Float,
    colors: List<Color>,
    rng: Random,
): ConfettiParticle {
    val shape = if (rng.nextFloat() < 0.6f) ConfettiShape.RECT else ConfettiShape.CIRCLE
    return ConfettiParticle(
        startX = rng.nextFloat() * screenW,
        startY = -(rng.nextFloat() * screenH * 0.15f + 10f),
        velY = rng.nextFloat() * 0.35f + 0.25f,          // 0.25 – 0.60 px/ms
        drift = (rng.nextFloat() - 0.5f) * 0.08f,
        color = colors[rng.nextInt(colors.size)],
        sizePx = rng.nextFloat() * 14f + 7f,              // 7 – 21 px
        shape = shape,
        aspectRatio = if (shape == ConfettiShape.RECT) rng.nextFloat() * 0.5f + 0.25f else 1f,
        startRotation = rng.nextFloat() * 360f,
        rotSpeed = (rng.nextFloat() - 0.5f) * 0.5f,      // ±0.25 deg/ms
        wobbleFreq = rng.nextFloat() * 0.003f + 0.001f,  // 0.001 – 0.004 rad/ms
        wobbleAmp = rng.nextFloat() * 60f + 20f,          // 20 – 80 px
        phase = rng.nextFloat() * 2f * PI.toFloat(),
        delay = rng.nextFloat() * 800f,                   // stagger 0 – 800 ms
    )
}

/**
 * A full-screen confetti shower that renders on top of your content without
 * intercepting any touch / pointer input.
 *
 * Place this as the **last child inside a [Box]** so it appears above the rest
 * of your UI but never blocks interactions with it.
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     YourScreenContent()
 *     ConfettiOverlay(active = showConfetti, onFinished = { showConfetti = false })
 * }
 * ```
 *
 * @param active      Start / stop the animation. Toggling from false → true
 *                    restarts a fresh burst.
 * @param colors      Confetti piece colors.
 * @param pieceCount  Number of particles per burst.
 * @param fadeMode    [ConfettiFadeMode.NONE] full-height fall;
 *                    [ConfettiFadeMode.BOTTOM_HALF] fades out in the lower half.
 * @param onFinished  Called once all particles have left the screen.
 */
@Composable
fun ConfettiOverlay(
    active: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = confettiDefaultColors,
    pieceCount: Int = 80,
    fadeMode: ConfettiFadeMode = ConfettiFadeMode.NONE,
    onFinished: () -> Unit = {},
) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var timeMs by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(active) {
        if (!active) {
            particles = emptyList()
            timeMs = 0f
            return@LaunchedEffect
        }

        // Wait until the Canvas has been measured at least once.
        snapshotFlow { screenSize }
            .filter { it.width > 0f && it.height > 0f }
            .first()
            .let { sz ->
                particles = List(pieceCount) {
                    createParticle(sz.width, sz.height, colors, Random.Default)
                }
                timeMs = 0f
            }

        // Frame loop — runs on the Compose main-thread dispatcher.
        var lastFrame = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            timeMs += (now - lastFrame).coerceAtMost(50L).toFloat()
            lastFrame = now
            if (particles.all { it.isFinished(timeMs, screenSize.height) }) {
                onFinished()
                particles = emptyList()
                break
            }
        }
    }

    // Reading these values here (composition scope) creates snapshot subscriptions.
    // Any change to timeMs or particles will trigger recomposition → Canvas redraw.
    val t = timeMs
    val currentParticles = particles

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // onSizeChanged feeds the initial screen dimensions to the particle factory.
            .onSizeChanged { sz ->
                screenSize = Size(sz.width.toFloat(), sz.height.toFloat())
            },
    ) {
        currentParticles.forEach { p ->
            if (p.isFinished(t, size.height)) return@forEach

            val x = p.xAt(t)
            val y = p.yAt(t)

            val alpha = when (fadeMode) {
                ConfettiFadeMode.NONE -> 1f
                ConfettiFadeMode.BOTTOM_HALF -> {
                    val progress = y / size.height
                    when {
                        progress < 0.5f -> 1f
                        progress > 0.8f -> 0f
                        else -> 1f - (progress - 0.5f) / 0.3f
                    }.coerceIn(0f, 1f)
                }
            }

            val color = p.color.copy(alpha = p.color.alpha * alpha)

            when (p.shape) {
                ConfettiShape.RECT -> {
                    val hw = p.sizePx * p.aspectRatio / 2f
                    val hh = p.sizePx / 2f
                    rotate(degrees = p.rotAt(t), pivot = Offset(x, y)) {
                        drawRect(
                            color = color,
                            topLeft = Offset(x - hw, y - hh),
                            size = Size(p.sizePx * p.aspectRatio, p.sizePx),
                        )
                    }
                }

                ConfettiShape.CIRCLE -> drawCircle(
                    color = color,
                    radius = p.sizePx / 2f,
                    center = Offset(x, y),
                )
            }
        }
    }
}
