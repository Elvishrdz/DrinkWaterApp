package com.eahm.drinkwaterapp.system.service.goalcelebration.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.DecorKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
)

private val bubbleSpecs = listOf(
    BubbleSpec(0.15f, 0.80f, 10.dp, 0),
    BubbleSpec(0.05f, 0.04f, 5.dp, 500),
    BubbleSpec(0.35f, 0.70f, 16.dp, 600),
    BubbleSpec(0.55f, 0.85f, 22.dp, 1200),
    BubbleSpec(0.75f, 0.75f, 26.dp, 1800),
    BubbleSpec(0.25f, 0.90f, 12.dp, 2400),
    BubbleSpec(0.65f, 0.65f, 18.dp, 3000),
)

@Composable
fun BubblesDecor(modifier: Modifier = Modifier) {
    val durationMs = 3000
    bubbleSpecs.forEach { spec ->
        val transition = rememberInfiniteTransition(label = "bubble_${spec.delayMs}")
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
            label = "bubble_progress_${spec.delayMs}",
        )

        val alpha = when {
            progress < 0.1f -> progress / 0.1f
            progress > 0.8f -> (1f - progress) / 0.2f
            else -> 1f
        }.coerceIn(0f, 1f)

        val translateY = ((-90).dp * progress)

        Box(
            modifier = modifier
                .drawBehind {
                    val x = size.width * spec.xFraction
                    val y = size.height * spec.yStartFraction + translateY.toPx()
                    val radius = spec.sizeDp.toPx() / 2f
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.4f),
                        radius = radius,
                        center = Offset(x, y),
                        style = Stroke(width = 1.5f),
                    )
                }
        )
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
