package com.eahm.drinkwaterapp.ui.drinks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Visual configuration for the liquid and its container.
 *
 * @param color         Main liquid body color.
 * @param waveHighlightColor Secondary wave drawn as a translucent layer behind the main wave,
 *                           creating an extra sense of depth and movement.
 * @param containerColor     Color of the empty part of the container.
 * @param waveAmplitude      Peak-to-trough height of the animated surface wave.
 * @param waveFrequency      Number of complete sine cycles visible across the container width.
 * @param waveSpeed          Animation speed multiplier (1.0 = normal, 2.0 = twice as fast).
 */
data class DrinkAppearance(
    val color: Color = Color(0xFF1C8EBF),
    val waveHighlightColor: Color = Color(0xFF4FB3DD),
    val containerColor: Color = Color(0xFFEAF4F9),
    val waveAmplitude: Dp = 4.dp,
    val waveFrequency: Float = 1.5f,
    val waveSpeed: Float = 3.5f,
)

/**
 * Configuration for bubble particles inside the liquid.
 *
 * Bubbles rise from the bottom of the water column toward the surface and are
 * respawned once they break through. Adjust these values to simulate different
 * liquids — e.g. slow large bubbles for thick syrup, many tiny fast ones for soda.
 *
 * @param enabled   Whether bubbles are rendered.
 * @param count     Number of simultaneously active bubbles.
 * @param color     Bubble color; include alpha for translucency (default is white at 35%).
 * @param minRadius Smallest bubble radius.
 * @param maxRadius Largest bubble radius.
 * @param speed     Rise-speed multiplier (1.0 = normal).
 */
data class BubbleConfig(
    val enabled: Boolean = true,
    val count: Int = 8,
    val color: Color = Color.White.copy(alpha = 0.35f),
    val minRadius: Dp = 3.dp,
    val maxRadius: Dp = 8.dp,
    val speed: Float = 1.0f,
)

/**
 * Defines the clipping shape for [DrinkShape].
 *
 * Five built-in shapes are available as singleton objects or data classes.
 * Use [Custom] to supply any arbitrary [Path] using pixel coordinates.
 *
 * All built-in shape coordinates are computed to exactly fill the composable's
 * measured bounds ([Size]).
 */
sealed class DrinkShape {

    /** Resolves this shape to a pixel-space [Path] for the given bounds. */
    internal abstract fun buildPath(size: Size, density: Density): Path

    /** Water-bottle silhouette — narrow neck flowing into a wide rounded body. */
    object Bottle : DrinkShape() {
        override fun buildPath(size: Size, density: Density) = buildBottlePath(size)
    }

    /** Circle inscribed in the composable's bounds. */
    object Circle : DrinkShape() {
        override fun buildPath(size: Size, density: Density) = buildCirclePath(size)
    }

    /** Ellipse that fills the composable's bounds. */
    object Oval : DrinkShape() {
        override fun buildPath(size: Size, density: Density) = buildOvalPath(size)
    }

    /** Classic water-drop silhouette — circle base with a pointed top. */
    object Drop : DrinkShape() {
        override fun buildPath(size: Size, density: Density) = buildDropPath(size)
    }

    /** Rectangle with uniform rounded corners. */
    data class RoundedRect(val cornerRadius: Dp = 20.dp) : DrinkShape() {
        override fun buildPath(size: Size, density: Density): Path {
            val r = with(density) { cornerRadius.toPx() }
            return buildRoundedRectPath(size, r)
        }
    }

    /**
     * Fully custom shape.
     *
     * @param builder Returns a [Path] whose coordinates are in pixels matching [Size].
     *                [Density] is provided so you can convert [Dp] values inside.
     */
    class Custom(val builder: (size: Size, density: Density) -> Path) : DrinkShape() {
        override fun buildPath(size: Size, density: Density) = builder(size, density)
    }
}

/**
 * Renders a liquid-fill animation clipped to a chosen [shape].
 *
 * The water surface is a continuous animated sine wave. Two overlapping waves —
 * one lighter and one darker — create a sense of depth and organic movement.
 * Bubbles rise through the liquid and respawn automatically.
 *
 * When [onFillChanged] is provided, the user can drag vertically anywhere on the
 * composable to raise or lower the liquid level interactively. External [fillFraction]
 * changes animate in with a spring.
 *
 * ```kotlin
 * var fill by remember { mutableFloatStateOf(0.5f) }
 *
 * DrinkShape(
 *     fillFraction = fill,
 *     shape        = WaterShape.Bottle,
 *     onFillChanged = { fill = it },
 *     modifier     = Modifier.size(200.dp, 300.dp),
 * )
 * ```
 *
 * @param fillFraction  Liquid level: **0.0** = empty, **1.0** = full.
 * @param modifier      Modifier applied to the composable's bounds.
 * @param shape         Container shape used for clipping.
 * @param appearance    Water and container visual configuration.
 * @param bubbles       Bubble particle configuration.
 * @param onFillChanged Invoked with the updated fill fraction as the user drags.
 *                      Pass `null` to disable drag interaction entirely.
 */
@Composable
fun DrinkShape(
    fillFraction: Float,
    modifier: Modifier = Modifier,
    shape: DrinkShape = DrinkShape.Bottle,
    appearance: DrinkAppearance = DrinkAppearance(),
    bubbles: BubbleConfig = BubbleConfig(),
    onFillChanged: ((Float) -> Unit)? = null,
) {
    val density = LocalDensity.current

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val shapePath = remember(shape, canvasSize) {
        if (canvasSize == Size.Zero) Path()
        else shape.buildPath(canvasSize, density)
    }

    // `targetFill` is updated immediately (drag / external change).
    // `animatedFill` follows it: snaps during drag for zero-latency feel,
    // springs otherwise for a smooth transition.
    var targetFill by remember { mutableFloatStateOf(fillFraction.coerceIn(0f, 1f)) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(fillFraction) {
        if (!isDragging) targetFill = fillFraction.coerceIn(0f, 1f)
    }

    val animatedFill by animateFloatAsState(
        targetValue = targetFill,
        animationSpec = if (isDragging) snap()
        else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "waterFill",
    )

    // Reading `timeMs` in composition scope creates a per-frame subscription that
    // drives wave-phase computation and Canvas redraws without additional state.
    var timeMs by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            timeMs += (now - last).coerceAtMost(50L).toFloat()
            last = now
        }
    }

    val bubbleList = remember { mutableStateListOf<BubbleData>() }
    val rng = remember { Random.Default }

    LaunchedEffect(canvasSize, bubbles) {
        if (canvasSize == Size.Zero || !bubbles.enabled) {
            bubbleList.clear()
            return@LaunchedEffect
        }
        val minR = with(density) { bubbles.minRadius.toPx() }
        val maxR = with(density) { bubbles.maxRadius.toPx() }

        // Stagger initial spawn times so bubbles don't all start at the same height.
        bubbleList.clear()
        val staggerMs = if (bubbles.count > 1) 3_000f / bubbles.count else 0f
        repeat(bubbles.count) { index ->
            bubbleList.add(
                spawnBubble(
                    currentTime = timeMs + index * staggerMs,
                    size = canvasSize,
                    fillFraction = targetFill,
                    minRadius = minR,
                    maxRadius = maxR,
                    speedMult = bubbles.speed,
                    rng = rng,
                )
            )
        }

        // Respawn loop: when a bubble reaches the water surface, send it back down.
        while (true) {
            withFrameMillis { }
            val t = timeMs
            val fill = targetFill
            val surfaceY = canvasSize.height * (1f - fill)
            for (i in bubbleList.indices) {
                val b = bubbleList[i]
                if (b.yAt(t) < surfaceY - b.radius) {
                    bubbleList[i] = spawnBubble(t, canvasSize, fill, minR, maxR, bubbles.speed, rng)
                }
            }
        }
    }

    val dragModifier = if (onFillChanged != null) {
        Modifier.pointerInput(onFillChanged) {
            detectVerticalDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onVerticalDrag = { _, dragAmount ->
                    // dragAmount > 0 → finger moved down → lower water level
                    // dragAmount < 0 → finger moved up   → raise water level
                    val delta = -dragAmount / size.height
                    targetFill = (targetFill + delta).coerceIn(0f, 1f)
                    onFillChanged(targetFill)
                },
            )
        }
    } else Modifier

    // Pre-compute Dp → px, derive wave phases from time
    val amplitudePx = with(density) { appearance.waveAmplitude.toPx() }

    // Subscribing to `timeMs` here drives per-frame recomposition → Canvas redraw.
    val t = timeMs
    val wave1Phase = (t * 0.0020f * appearance.waveSpeed) % TWO_PI
    // Slightly different period so the two waves are never perfectly in sync.
    val wave2Phase = (t * 0.0014f * appearance.waveSpeed) % TWO_PI

    // Reusable path objects: allocated once per composable instance, reset each frame.
    val wavePath1 = remember { Path() }
    val wavePath2 = remember { Path() }

    Canvas(
        modifier = modifier
            .then(dragModifier)
            .onSizeChanged { sz ->
                canvasSize = Size(sz.width.toFloat(), sz.height.toFloat())
            },
    ) {
        // y-coordinate of the water surface (0 = top of canvas, h = bottom).
        val fillY = size.height * (1f - animatedFill)

        clipPath(shapePath) {

            // 1. Empty-container background.
            drawRect(color = appearance.containerColor, size = size)

            // 2. Back highlight wave — runs at a slightly different phase and sits
            //    slightly higher than the main wave, drawn with reduced opacity.
            drawWaveFill(
                path = wavePath1,
                fillY = fillY + amplitudePx * 0.5f,
                phase = wave2Phase + PI.toFloat(),   // opposite direction
                amplitude = amplitudePx * 0.65f,
                frequency = appearance.waveFrequency,
                color = appearance.waveHighlightColor.copy(alpha = 0.55f),
                canvasW = size.width,
                canvasH = size.height,
            )

            // 3. Front main wave.
            drawWaveFill(
                path = wavePath2,
                fillY = fillY,
                phase = wave1Phase,
                amplitude = amplitudePx,
                frequency = appearance.waveFrequency,
                color = appearance.color,
                canvasW = size.width,
                canvasH = size.height,
            )

            // 4. Bubbles only rendered below the animated water surface.
            if (bubbles.enabled) {
                val surfaceY = size.height * (1f - animatedFill)
                bubbleList.forEach { b ->
                    val by = b.yAt(t)
                    if (by > surfaceY) {
                        drawCircle(
                            color = bubbles.color,
                            radius = b.radius,
                            center = Offset(b.xAt(t), by),
                        )
                    }
                }
            }
        }
    }
}

private const val TWO_PI = (PI * 2).toFloat()
private const val WAVE_STEP_PX = 4f   // horizontal sample resolution (pixels)

/**
 * Draws a sine-wave surface with a solid fill from the wave down to the canvas bottom.
 * [path] is reset and rebuilt every call; pass a pre-allocated [Path] to avoid allocation.
 */
private fun DrawScope.drawWaveFill(
    path: Path,
    fillY: Float,
    phase: Float,
    amplitude: Float,
    frequency: Float,
    color: Color,
    canvasW: Float,
    canvasH: Float,
) {
    path.reset()
    path.moveTo(0f, fillY + amplitude * sin(phase))
    var x = WAVE_STEP_PX
    while (x <= canvasW) {
        path.lineTo(x, fillY + amplitude * sin(TWO_PI * frequency * x / canvasW + phase))
        x += WAVE_STEP_PX
    }
    // Seal the water area by closing along the bottom edge.
    path.lineTo(canvasW, canvasH)
    path.lineTo(0f, canvasH)
    path.close()
    drawPath(path, color)
}

/**
 * Immutable bubble descriptor. Position is computed deterministically from [birthTime],
 * so no mutable state is needed per particle — only [timeMs] drives updates.
 */
private data class BubbleData(
    val startX: Float,
    val startY: Float,
    val birthTime: Float,
    val speedPxPerMs: Float,
    val radius: Float,
    val wobbleAmp: Float,    // horizontal sway amplitude (px)
    val wobbleFreq: Float,   // horizontal sway frequency (rad/ms)
    val phase: Float,
) {
    /** Current y position (rises over time; lower values = higher on screen). */
    fun yAt(t: Float): Float = startY - speedPxPerMs * (t - birthTime)

    /** Current x position with gentle horizontal wobble. */
    fun xAt(t: Float): Float = startX + wobbleAmp * sin(wobbleFreq * (t - birthTime) + phase)
}

private fun spawnBubble(
    currentTime: Float,
    size: Size,
    fillFraction: Float,
    minRadius: Float,
    maxRadius: Float,
    speedMult: Float,
    rng: Random,
): BubbleData {
    val radius = rng.nextFloat() * (maxRadius - minRadius) + minRadius
    val surfaceY = size.height * (1f - fillFraction)
    val waterH = (size.height - surfaceY).coerceAtLeast(1f)

    return BubbleData(
        // Spawn in the bottom third of the water column so bubbles have room to rise.
        startX = radius + rng.nextFloat() * (size.width - 2f * radius),
        startY = size.height - rng.nextFloat() * waterH * 0.3f,
        birthTime = currentTime,
        speedPxPerMs = (rng.nextFloat() * 0.04f + 0.02f) * speedMult,  // 0.02 – 0.06 px/ms
        radius = radius,
        wobbleAmp = rng.nextFloat() * 8f + 2f,
        wobbleFreq = rng.nextFloat() * 0.005f + 0.002f,
        phase = rng.nextFloat() * TWO_PI,
    )
}

/**
 * Bottle silhouette: narrow neck with rounded top, smooth cubic-bezier shoulders
 * flowing into a wide body with strongly rounded bottom corners.
 *
 * Arc angle verification (all in degrees, clockwise from positive-x):
 *   top-right neck corner  : 270° → 360°  (top of oval → right of oval)
 *   bottom-right body corner: 0°  → 90°   (right → bottom)
 *   bottom-left  body corner: 90° → 180°  (bottom → left)
 *   top-left  neck corner  : 180° → 270°  (left → top)
 */
private fun buildBottlePath(size: Size): Path {
    val w = size.width
    val h = size.height

    val neckW = w * 0.30f        // neck width
    val neckH = h * 0.12f        // neck height (y where neck meets shoulder)
    val neckR = neckW * 0.45f    // neck corner radius
    val bodyR = w * 0.20f        // body corner radius
    val bodyTop = h * 0.18f        // y where body side edges become vertical

    val nx1 = (w - neckW) / 2f    // neck left x
    val nx2 = (w + neckW) / 2f    // neck right x

    return Path().apply {
        // Neck
        moveTo(nx1 + neckR, 0f)
        lineTo(nx2 - neckR, 0f)
        // Top-right neck corner: 270° → 360°
        arcTo(Rect(nx2 - 2f * neckR, 0f, nx2, 2f * neckR), 270f, 90f, false)
        lineTo(nx2, neckH)

        // Right shoulder
        cubicTo(
            nx2, neckH + (bodyTop - neckH) * 0.5f,   // ease down from neck
            w, bodyTop - bodyR * 0.5f,               // approach body corner
            w, bodyTop + bodyR,                       // arrive at body side
        )
        lineTo(w, h - bodyR)

        // Bottom-right corner: 0° → 90°
        arcTo(Rect(w - 2f * bodyR, h - 2f * bodyR, w, h), 0f, 90f, false)
        lineTo(bodyR, h)

        // Bottom-left corner: 90° → 180°
        arcTo(Rect(0f, h - 2f * bodyR, 2f * bodyR, h), 90f, 90f, false)
        lineTo(0f, bodyTop + bodyR)

        // Left shoulder
        cubicTo(
            0f, bodyTop - bodyR * 0.5f,
            nx1, neckH + (bodyTop - neckH) * 0.5f,
            nx1, neckH,
        )
        lineTo(nx1, neckR)

        // Top-left neck corner: 180° → 270°
        arcTo(Rect(nx1, 0f, nx1 + 2f * neckR, 2f * neckR), 180f, 90f, false)
        close()
    }
}

private fun buildCirclePath(size: Size): Path {
    val r = min(size.width, size.height) / 2f
    val cx = size.width / 2f
    val cy = size.height / 2f
    return Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
}

private fun buildOvalPath(size: Size): Path =
    Path().apply { addOval(Rect(0f, 0f, size.width, size.height)) }

/**
 * Water-drop: a circle base (radius = half the width) tangent to both sides, with
 * two symmetric cubic beziers forming the pointed top.
 */
private fun buildDropPath(size: Size): Path {
    val cx = size.width / 2f
    val r = size.width / 2f   // circle radius = half width
    val cy = size.height - r   // circle centre y

    return Path().apply {
        moveTo(cx, 0f)  // tip
        // Right side: tip → rightmost circle point
        cubicTo(
            cx + r * 0.55f, size.height * 0.20f,
            cx + r, cy - r * 0.55f,
            cx + r, cy,
        )
        // Bottom arc (right → bottom → left): 0° → 180°
        arcTo(Rect(0f, cy - r, size.width, cy + r), 0f, 180f, false)
        // Left side: leftmost circle point → tip
        cubicTo(
            cx - r, cy - r * 0.55f,
            cx - r * 0.55f, size.height * 0.20f,
            cx, 0f,
        )
        close()
    }
}

private fun buildRoundedRectPath(size: Size, cornerRadius: Float): Path =
    Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                radiusX = cornerRadius,
                radiusY = cornerRadius,
            )
        )
    }
