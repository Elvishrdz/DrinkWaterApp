package com.eahm.drinkwaterapp.feature.home.today

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito

@Composable
fun ProgressRing(
    percent: Float,
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
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
            Text(
                text = "${animatedPercent.toInt()}%",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.08.sp,
                color = DwWater
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = DwInk900
                        )
                    ) {
                        append(current.toString())
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DwInk400
                        )
                    ) {
                        append(" mL")
                    }
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "of ${goal.toLocaleString()} mL",
                style = MaterialTheme.typography.bodySmall,
                color = DwInk400
            )
        }
    }
}

private fun Int.toLocaleString(): String = "%,d".format(this).replace(',', ' ')

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun ProgressRingPreview() {
    DrinkWaterAppTheme {
        ProgressRing(percent = 62f, current = 1500, goal = 2400)
    }
}
