package com.eahm.drinkwaterapp.system.service.goalcelebration.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.DecorKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DecorLayer(
    decorKind: DecorKind,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) { /* consume no events */ }
    ) {
        when (decorKind) {
            DecorKind.BUBBLES -> BubblesDecor(
                modifier = Modifier.matchParentSize(),
                config = BubblesConfig(
                    count = 10,
                    minSizeDp = 11f,
                    maxSizeDp = 34f,
                    minDurationMs = 3195,
                    maxDurationMs = 3354,
                    riseDistanceDp = 171f,
                    maxFillAlpha = 0.24f,
                    maxBorderAlpha = 0.40f,
                    seed = 64,
                ),
            )

            DecorKind.CONFETTI -> ConfettiDecor(
                modifier = Modifier.matchParentSize(),
            )

            DecorKind.SPARKLES -> SparklesDecor()
            DecorKind.DRIPS -> DripsDecor()
            DecorKind.RAYS -> RaysDecor()
            DecorKind.RINGS -> RingsDecor()
        }
    }
}

// ---------------------------------------------------------------------------
// Bubble data
// ---------------------------------------------------------------------------

private data class BubbleSpec(
    val xFraction: Float,
    val yStartFraction: Float,
    val sizeDp: Dp,
    val delayMs: Int,
    val durationMs: Int,
)

/**
 * Controls how bubbles are generated and animated in [BubblesDecor].
 *
 * @param count        Number of simultaneous bubbles.
 * @param minSizeDp    Smallest bubble diameter in dp.
 * @param maxSizeDp    Largest bubble diameter in dp.
 * @param minDurationMs  Fastest a single bubble can rise (ms). Lower = faster.
 * @param maxDurationMs  Slowest a single bubble can rise (ms).
 * @param riseDistanceDp How far up (in dp) each bubble travels before resetting.
 * @param maxBorderAlpha     Peak opacity of the stroke (0–1). Final alpha is multiplied by this.
 * @param seed         Random seed — change to get a different spatial layout.
 */
data class BubblesConfig(
    val count: Int = 7,
    val minSizeDp: Float = 5f,
    val maxSizeDp: Float = 26f,
    val minDurationMs: Int = 2000,
    val maxDurationMs: Int = 4500,
    val riseDistanceDp: Float = 90f,
    val maxBorderAlpha: Float = 0.3f,
    val maxFillAlpha: Float = 0.5f,
    val seed: Long = 42L,
)

@Composable
fun BubblesDecor(
    modifier: Modifier = Modifier,
    config: BubblesConfig = BubblesConfig(),
) {
    val specs = remember(config) {
        val rng = Random(config.seed)
        val cycleMs = 6000
        List(config.count) { i ->
            BubbleSpec(
                xFraction = rng.nextFloat() * 0.85f + 0.05f,
                yStartFraction = rng.nextFloat() * 0.30f + 0.65f,
                sizeDp = (rng.nextFloat() * (config.maxSizeDp - config.minSizeDp) + config.minSizeDp).dp,
                delayMs = if (config.count > 1) (i * cycleMs / config.count) else 0,
                durationMs = rng.nextInt(config.minDurationMs, config.maxDurationMs + 1),
            )
        }
    }

    specs.forEachIndexed { index, spec ->
        key(index) {
            val transition = rememberInfiniteTransition(label = "bubble_$index")
            val progress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = spec.durationMs,
                        delayMillis = spec.delayMs,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "bubble_progress_$index",
            )

            val alpha = when {
                progress < 0.1f -> progress / 0.1f
                progress > 0.8f -> (1f - progress) / 0.2f
                else -> 1f
            }.coerceIn(0f, 1f)

            val translateY = (-config.riseDistanceDp.dp * progress)

            Box(
                modifier = modifier
                    .drawBehind {
                        val x = size.width * spec.xFraction
                        val y = size.height * spec.yStartFraction + translateY.toPx()
                        val radius = spec.sizeDp.toPx() / 2f

                        // fill
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * config.maxFillAlpha),
                            radius = radius,
                            center = Offset(x, y),
                        )

                        // border
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * config.maxBorderAlpha),
                            radius = radius,
                            center = Offset(x, y),
                            style = Stroke(width = 1.5f),
                        )
                    }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Confetti data
// ---------------------------------------------------------------------------

private data class ConfettiSpec(
    val xFraction: Float,
    val color: Color,
    val delayMs: Int,
)

private val confettiColors = listOf(
    Color(0xFFFFC107), Color(0xFFFF5252), Color(0xFF69F0AE),
    Color(0xFF40C4FF), Color(0xFFEA80FC), Color(0xFFFFFF00),
    Color(0xFFFF6D00),
)

private val confettiSpecs = (0 until 14).map { i ->
    ConfettiSpec(
        xFraction = (i / 13f) * 0.9f + 0.05f,
        color = confettiColors[i % confettiColors.size],
        delayMs = (i * 185),
    )
}

@Composable
fun ConfettiDecor(modifier: Modifier = Modifier) {
    val durationMs = 2600
    confettiSpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "confetti_${spec.delayMs}")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "confetti_progress_${spec.delayMs}",
        )

        Box(
            modifier = modifier
                .drawBehind {
                    val x = size.width * spec.xFraction
                    val y = progress * (size.height * 0.85f)
                    val rotation = progress * 360f
                    val alpha = when {
                        progress < 0.05f -> progress / 0.05f
                        progress > 0.85f -> (1f - progress) / 0.15f
                        else -> 1f
                    }.coerceIn(0f, 1f)

                    val w = 8.dp.toPx()
                    val h = 12.dp.toPx()

                    withTransform({
                        translate(x, y)
                        rotate(rotation, Offset.Zero)
                    }) {
                        drawRect(
                            color = spec.color.copy(alpha = alpha * 0.85f),
                            topLeft = Offset(-w / 2f, -h / 2f),
                            size = Size(w, h),
                        )
                    }
                }
        )
    }
}

// ---------------------------------------------------------------------------
// Sparkles data
// ---------------------------------------------------------------------------

private data class SparkleSpec(
    val xFraction: Float,
    val yFraction: Float,
    val delayMs: Int,
)

private val sparkleSpecs = listOf(
    SparkleSpec(0.10f, 0.20f, 0),
    SparkleSpec(0.30f, 0.50f, 320),
    SparkleSpec(0.55f, 0.15f, 640),
    SparkleSpec(0.70f, 0.70f, 960),
    SparkleSpec(0.85f, 0.35f, 1280),
    SparkleSpec(0.20f, 0.75f, 1600),
    SparkleSpec(0.65f, 0.45f, 1920),
)

private fun starPath(cx: Float, cy: Float, outerR: Float): Path {
    val path = Path()
    val innerR = outerR * 0.4f
    val points = 4
    for (i in 0 until points * 2) {
        val angle = (i * PI / points - PI / 2).toFloat()
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
fun SparklesDecor() {
    val durationMs = 2200
    sparkleSpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "sparkle_${spec.delayMs}")
        val scale by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sparkle_scale_${spec.delayMs}",
        )
        val alpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sparkle_alpha_${spec.delayMs}",
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    val cx = size.width * spec.xFraction
                    val cy = size.height * spec.yFraction
                    val outerR = 7.dp.toPx() * scale
                    val path = starPath(cx, cy, outerR)
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = alpha * 0.9f),
                    )
                }
        )
    }
}

// ---------------------------------------------------------------------------
// Drips data
// ---------------------------------------------------------------------------

private data class DripSpec(
    val xFraction: Float,
    val delayMs: Int,
)

private val dripSpecs = (0 until 10).map { i ->
    DripSpec(
        xFraction = (i / 9f) * 0.85f + 0.075f,
        delayMs = i * 180,
    )
}

@Composable
fun DripsDecor() {
    val durationMs = 1800
    dripSpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "drip_${spec.delayMs}")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "drip_progress_${spec.delayMs}",
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    val x = size.width * spec.xFraction
                    val y = progress * size.height * 0.80f
                    val alpha = when {
                        progress < 0.08f -> progress / 0.08f
                        progress > 0.75f -> (1f - progress) / 0.25f
                        else -> 1f
                    }.coerceIn(0f, 1f)
                    val w = 4.dp.toPx()
                    val h = 14.dp.toPx()
                    drawRoundRect(
                        color = Color.White.copy(alpha = alpha * 0.5f),
                        topLeft = Offset(x - w / 2f, y),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(w / 2f),
                    )
                }
        )
    }
}

// ---------------------------------------------------------------------------
// Rays data
// ---------------------------------------------------------------------------

private data class RaySpec(
    val angleDeg: Float,
    val delayMs: Int,
)

private val raySpecs = (0 until 9).map { i ->
    RaySpec(
        angleDeg = i * 40f,
        delayMs = i * 333,
    )
}

@Composable
fun RaysDecor() {
    val durationMs = 3000
    raySpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "ray_${spec.delayMs}")
        val alpha by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.90f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "ray_alpha_${spec.delayMs}",
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.35f
                    val rad = spec.angleDeg * PI.toFloat() / 180f
                    val length = (size.width.coerceAtLeast(size.height)) * 0.6f
                    val endX = cx + length * cos(rad)
                    val endY = cy + length * sin(rad)
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.4f),
                        start = Offset(cx, cy),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
        )
    }
}

// ---------------------------------------------------------------------------
// Rings data
// ---------------------------------------------------------------------------

private data class RingSpec(
    val radiusFraction: Float,
    val delayMs: Int,
)

private val ringSpecs = listOf(
    RingSpec(0.25f, 0),
    RingSpec(0.38f, 500),
    RingSpec(0.52f, 1000),
)

@Composable
fun RingsDecor() {
    val durationMs = 3200
    ringSpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "ring_${spec.delayMs}")
        val scale by transition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "ring_scale_${spec.delayMs}",
        )
        val alpha by transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = spec.delayMs,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "ring_alpha_${spec.delayMs}",
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.35f
                    val radius = size.width * spec.radiusFraction * scale
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.3f),
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
        )
    }
}

// ---------------------------------------------------------------------------
// Bubbles playground — use Interactive Preview (▶) in Android Studio to tune
// ---------------------------------------------------------------------------

/**
 * Drag the sliders to find a nice [BubblesConfig], then copy the values
 * into [BubblesConfig]'s defaults. Open this in Android Studio's
 * Interactive Preview mode (the ▶ play icon on the preview panel) so the
 * sliders are actually interactive.
 */
@Composable
fun BubblesDecorPlayground(modifier: Modifier = Modifier) {
    var count by remember { mutableIntStateOf(7) }
    var minSizeDp by remember { mutableFloatStateOf(5f) }
    var maxSizeDp by remember { mutableFloatStateOf(26f) }
    var minDurationMs by remember { mutableIntStateOf(2000) }
    var maxDurationMs by remember { mutableIntStateOf(4500) }
    var riseDistanceDp by remember { mutableFloatStateOf(90f) }
    var maxFillAlpha by remember { mutableFloatStateOf(0.2f) }
    var maxBorderAlpha by remember { mutableFloatStateOf(0.4f) }
    var seed by remember { mutableIntStateOf(42) }

    val config = BubblesConfig(
        count = count,
        minSizeDp = minSizeDp,
        maxSizeDp = maxSizeDp.coerceAtLeast(minSizeDp + 1f),
        minDurationMs = minDurationMs,
        maxDurationMs = maxDurationMs.coerceAtLeast(minDurationMs + 100),
        riseDistanceDp = riseDistanceDp,
        maxFillAlpha = maxFillAlpha,
        maxBorderAlpha = maxBorderAlpha,
        seed = seed.toLong(),
    )

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF1C8EBF))
        ) {
            BubblesDecor(
                modifier = Modifier.matchParentSize(),
                config = config,
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Count: $count")
            Slider(
                value = count.toFloat(),
                onValueChange = { count = it.roundToInt().coerceIn(1, 20) },
                valueRange = 1f..20f,
            )

            Text("Min size: ${"%.1f".format(minSizeDp)} dp")
            Slider(
                value = minSizeDp,
                onValueChange = { minSizeDp = it },
                valueRange = 2f..40f,
            )

            Text("Max size: ${"%.1f".format(maxSizeDp)} dp")
            Slider(
                value = maxSizeDp,
                onValueChange = { maxSizeDp = it },
                valueRange = 2f..60f,
            )

            Text("Min duration: $minDurationMs ms  (lower = faster)")
            Slider(
                value = minDurationMs.toFloat(),
                onValueChange = { minDurationMs = it.roundToInt() },
                valueRange = 500f..8000f,
            )

            Text("Max duration: $maxDurationMs ms")
            Slider(
                value = maxDurationMs.toFloat(),
                onValueChange = { maxDurationMs = it.roundToInt() },
                valueRange = 500f..8000f,
            )

            Text("Rise distance: ${"%.0f".format(riseDistanceDp)} dp")
            Slider(
                value = riseDistanceDp,
                onValueChange = { riseDistanceDp = it },
                valueRange = 20f..300f,
            )

            Text("Max fill alpha: ${"%.2f".format(maxFillAlpha)}")
            Slider(
                value = maxFillAlpha,
                onValueChange = { maxFillAlpha = it },
                valueRange = 0f..1f,
            )

            Text("Max border alpha: ${"%.2f".format(maxBorderAlpha)}")
            Slider(
                value = maxBorderAlpha,
                onValueChange = { maxBorderAlpha = it },
                valueRange = 0.05f..1f,
            )

            Text("Seed: $seed  (changes bubble layout)")
            Slider(
                value = seed.toFloat(),
                onValueChange = { seed = it.roundToInt() },
                valueRange = 0f..100f,
            )
        }
    }
}

@Preview(
    name = "Bubbles Playground",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
)
@Composable
private fun PreviewBubblesPlayground() {
    DrinkWaterAppTheme {
        BubblesDecorPlayground(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "Bubbles (Ocean)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewBubbles() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFF1C8EBF))
    ) {
        BubblesDecor()
    }
}

@Preview(name = "Confetti (Carnival)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewConfetti() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFFB83A95))
    ) {
        ConfettiDecor()
    }
}

@Preview(name = "Sparkles (Starlight)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewSparkles() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFF0E2235))
    ) {
        SparklesDecor()
    }
}

@Preview(name = "Drips (Rain)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewDrips() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFF2BA89A))
    ) {
        DripsDecor()
    }
}

@Preview(name = "Rays (Sunset)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewRays() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFF1C8EBF))
    ) {
        RaysDecor()
    }
}

@Preview(name = "Rings (Moonlit)", widthDp = 200, heightDp = 200)
@Composable
private fun PreviewRings() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFF008577))
    ) {
        RingsDecor()
    }
}
