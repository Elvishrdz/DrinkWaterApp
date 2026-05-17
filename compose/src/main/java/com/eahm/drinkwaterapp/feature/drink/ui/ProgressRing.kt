package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.domain.extension.sanitizeZeroDecimals
import kotlin.math.roundToInt

@Composable
fun ProgressRing(
    current: Float,
    goal: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    displayZeroPercentage: Boolean = true,
    onClick: (() -> Unit)? = null,
    bounceEnabled: Boolean = true,
    rippleEnabled: Boolean = false,
    idleShimmerEnabled: Boolean = true,
) {
    val percent = ((current / goal) * 100f)
        .takeIf { !it.isNaN() } ?: 0f
        .coerceIn(0f, 100f)

    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .idleShimmer(enabled = idleShimmerEnabled && current == 0f)
            .bounceTap(
                onClick = { onClick?.invoke() },
                enabled = bounceEnabled,
                rippleEnabled = rippleEnabled,
            ),
    ) {
        val strokeWidth = 14.dp
        val trackColor = DwWater.copy(alpha = 0.15f)
        val progressBrush = Brush.verticalGradient(
            colors = listOf(DwWaterLight, DwWaterDeep)
        )

        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val sweepAngle = (animatedPercent / 100f) * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val percentage = runCatching { animatedPercent.roundToInt() }
                .getOrDefault(0)
                .takeIf { if (displayZeroPercentage) it >= 0 else it > 0 }

            Text(
                text = "${percentage}%",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.08.sp,
                color = MaterialTheme.colorScheme.secondary.copy(
                    alpha = if (percentage != null) 1f else 0f
                ),
            )

            Spacer(modifier = Modifier.height(2.dp))

            val mlUnit = stringResource(R.string.volume_unit_mililiters)
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    ) {
                        append(current.sanitizeZeroDecimals())
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    ) {
                        append(" $mlUnit")
                    }
                },
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(
                    R.string.drink_progress_goal_label,
                    goal.sanitizeZeroDecimals()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = DwInk400
            )
        }
    }
}

@Preview
@Composable
private fun ProgressRingPreview() {
    DrinkWaterAppTheme {
        ProgressRing(current = 1500f, goal = 2400f)
    }
}
