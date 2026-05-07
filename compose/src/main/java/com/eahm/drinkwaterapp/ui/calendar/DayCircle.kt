package com.eahm.drinkwaterapp.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

@Composable
fun DayCircle(
    day: CalendarDayData,
    today: LocalDate,
    config: DrinkCalendarConfig = DrinkCalendarDefaults.config(),
    modifier: Modifier = Modifier,
) {
    val isToday = day.date == today
    val circleSize = config.dayCircleSize

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(circleSize),
        ) {
            CircleCanvas(
                displayState = day.displayState,
                progress = day.progress,
                size = circleSize,
                config = config,
            )

            val textColor = if (day.displayState == DayDisplayState.BEFORE_FIRST_RECORD) {
                config.dayTextStyle.color.copy(alpha = config.disabledAlpha)
            } else {
                config.dayTextStyle.color
            }
            Text(
                text = day.date.day.toString(),
                style = config.dayTextStyle.copy(color = textColor),
            )
        }

        if (isToday) {
            Spacer(modifier = Modifier.height(2.dp))
            Canvas(modifier = Modifier.size(4.dp)) {
                drawCircle(color = config.todayIndicatorColor)
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun MonthCircle(
    month: CalendarMonthData,
    config: DrinkCalendarConfig = DrinkCalendarDefaults.config(),
    label: String = "",
    modifier: Modifier = Modifier,
) {
    val circleSize = config.monthCircleSize

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(circleSize),
        ) {
            CircleCanvas(
                displayState = month.displayState,
                progress = month.progress,
                size = circleSize,
                config = config,
            )
        }

        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = config.dayTextStyle,
            )
        }
    }
}

@Composable
private fun CircleCanvas(
    displayState: DayDisplayState,
    progress: Float,
    size: Dp,
    config: DrinkCalendarConfig,
) {
    if (displayState == DayDisplayState.BEFORE_FIRST_RECORD ||
        displayState == DayDisplayState.FUTURE
    ) return

    val strokeWidth = config.strokeWidth
    val progressBrush = Brush.verticalGradient(colors = config.progressColors)
    val goalBrush = Brush.verticalGradient(colors = config.goalReachedColors)

    Canvas(modifier = Modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        val topLeft = Offset(inset, inset)

        when (displayState) {
            DayDisplayState.NO_DATA -> {
                drawArc(
                    color = config.trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }

            DayDisplayState.PARTIAL -> {
                drawArc(
                    color = config.trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                val sweepAngle = progress * 360f
                if (sweepAngle > 0f) {
                    drawArc(
                        brush = progressBrush,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }
            }

            DayDisplayState.GOAL_REACHED -> {
                drawArc(
                    brush = goalBrush,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                )
            }

            else -> Unit
        }
    }
}
