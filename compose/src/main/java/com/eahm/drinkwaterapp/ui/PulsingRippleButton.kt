package com.eahm.drinkwaterapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RippleData(
    val id: Long,
    var radiusPx: Float,
    var progress: Float
)

class RippleMotor(val config: RippleButtonConfig, val startRadiusPx: Float) {
    val ripples = mutableListOf<RippleData>()
    var lastFrameMs = 0L
    var lastSpawnMs = 0L

    fun tick(nowMs: Long) {
        val dtSec = if (lastFrameMs == 0L) 0f
        else ((nowMs - lastFrameMs) / 1000f).coerceAtMost(0.016f)
        lastFrameMs = nowMs

        if (nowMs - lastSpawnMs >= config.rippleFrequencyMs && ripples.size < config.maxRipples) {
            ripples.add(RippleData(id = nowMs, radiusPx = startRadiusPx, progress = 0f))
            lastSpawnMs = nowMs
        }

        val reachPx = config.rippleReachPx
        val iter = ripples.iterator()
        while (iter.hasNext()) {
            val rp = iter.next()
            rp.radiusPx += config.rippleSpeedPxPerSec * dtSec
            rp.progress = ((rp.radiusPx - startRadiusPx) / reachPx).coerceIn(0f, 1f)
            if (rp.progress >= 1f) iter.remove()
        }
    }

    fun spawn() {
        ripples.add(RippleData(id = System.nanoTime(), radiusPx = startRadiusPx, progress = 0f))
    }
}

enum class RippleFadeCurve { Linear, EaseOut, EaseOutStrong, LateFade }

data class RippleButtonConfig(
    val buttonSizeDp: Dp = 100.dp,
    val buttonColor: Color = Color(0xFF4DAEFF),
    val textColor: Color = Color.White,
    val fontSize: TextUnit = 18.sp,
    val fontWeight: FontWeight = FontWeight.Bold,

    val rippleColorInner: Color = Color(0xFF5EC8E8),
    val rippleColorOuter: Color = Color(0xFF2196F3),
    val rippleBlurRadiusDp: Float = 8f,      // Nuevo: desenfoque suave

    val rippleFrequencyMs: Long = 1200L,
    val rippleSpeedPxPerSec: Float = 180f,
    val rippleReachPx: Float = 400f,
    val rippleStrokeWidthPx: Float = 60f,    // Ancho del anillo
    val rippleInitialAlpha: Float = 0.75f,
    val maxRipples: Int = 8,
    val fadeCurve: RippleFadeCurve = RippleFadeCurve.EaseOut,
)


@Composable
fun PulsingRippleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    config: RippleButtonConfig = RippleButtonConfig(),
) {
    val density = LocalDensity.current
    val buttonRadiusPx = with(density) { (config.buttonSizeDp.value / 2).dp.toPx() }
    val blurRadiusPx = with(density) { config.rippleBlurRadiusDp.dp.toPx() }

    val rippleState = remember { RippleMotor(config, buttonRadiusPx) }
    val frameTime = remember { mutableLongStateOf(0L) }

    LaunchedEffect(config.rippleFrequencyMs) {
        while (true) {
            withFrameMillis { frameMs ->
                rippleState.tick(frameMs)
                frameTime.longValue = frameMs
            }
        }
    }

    val totalSizeDp = config.buttonSizeDp + with(density) {
        (config.rippleReachPx * 2).toDp()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(totalSizeDp)
    ) {
        val frame = frameTime.longValue
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION") frame

            val cx = size.width / 2f
            val cy = size.height / 2f

            // Dibujar ripples — simple, limpio, sin artefactos
            for (rp in rippleState.ripples) {
                val alpha = config.rippleInitialAlpha * fadeFactor(rp.progress, config.fadeCurve)
                val blendColor = lerpColor(
                    config.rippleColorInner,
                    config.rippleColorOuter,
                    rp.progress
                )

                drawSoftRipple(
                    cx = cx,
                    cy = cy,
                    radiusPx = rp.radiusPx,
                    color = blendColor,
                    alpha = alpha,
                    strokeWidthPx = config.rippleStrokeWidthPx,
                    blurRadiusPx = blurRadiusPx,
                )
            }

            drawCircle(
                color = config.buttonColor,
                radius = buttonRadiusPx,
                center = Offset(cx, cy)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(config.buttonSizeDp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    rippleState.spawn()
                    onClick()
                }
        ) {
            Text(
                text = text,
                color = config.textColor,
                fontSize = config.fontSize,
                fontWeight = config.fontWeight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun DrawScope.drawSoftRipple(
    cx: Float,
    cy: Float,
    radiusPx: Float,
    color: Color,
    alpha: Float,
    strokeWidthPx: Float,
    blurRadiusPx: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to color.copy(alpha = 0f),
                0.3f to color.copy(alpha = (alpha * 0.4f).coerceIn(0f, 1f)),
                0.5f to color.copy(alpha = alpha.coerceIn(0f, 1f)),
                0.7f to color.copy(alpha = (alpha * 0.4f).coerceIn(0f, 1f)),
                1.0f to color.copy(alpha = 0f)
            ),
            center = Offset(cx, cy),
            radius = radiusPx + strokeWidthPx * 0.5f + blurRadiusPx
        ),
        radius = radiusPx,
        center = Offset(cx, cy)
    )
}

private fun fadeFactor(progress: Float, curve: RippleFadeCurve): Float {
    return when (curve) {
        RippleFadeCurve.Linear -> 1f - progress
        RippleFadeCurve.EaseOut -> 1f - progress * progress
        RippleFadeCurve.EaseOutStrong -> (1f - progress).let { it * it * it }
        RippleFadeCurve.LateFade ->
            if (progress < 0.55f) 1f else 1f - ((progress - 0.55f) / 0.45f)
    }
}

private fun lerpColor(start: Color, end: Color, progress: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * progress,
        green = start.green + (end.green - start.green) * progress,
        blue = start.blue + (end.blue - start.blue) * progress,
        alpha = start.alpha + (end.alpha - start.alpha) * progress
    )
}

@Preview
@Composable
private fun Preview() {
    PulsingRippleButton(
        text = "Text",
        onClick = {},
        config = RippleButtonConfig(
            maxRipples = 8,
            fadeCurve = RippleFadeCurve.EaseOutStrong,
        ),
    )
}