package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSlot
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import kotlinx.datetime.LocalDateTime

@Composable
internal fun HomeScheduleTimeline(
    schedule: DrinkSchedule,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    now: LocalDateTime,
    isInActiveHours: Boolean,
    is24Hour: Boolean = true,
) {
    val slots = schedule.slots
    val startMin = startTime.hour * 60 + startTime.minute
    val endMin = endTime.hour * 60 + endTime.minute
    val totalMin = if (endMin > startMin) {
        (endMin - startMin).toFloat()
    } else {
        (24 * 60 - startMin + endMin).toFloat()
    }

    val nowMin = now.hour * 60 + now.minute
    val nowMinFromStart = if (nowMin >= startMin) {
        (nowMin - startMin).toFloat()
    } else {
        (24 * 60 - startMin + nowMin).toFloat()
    }
    val nowFraction = if (totalMin > 0f) (nowMinFromStart / totalMin).coerceIn(0f, 1f) else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Time range labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = startTime.formatTime(is24Hour),
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = DwInk400,
            )
            Text(
                text = endTime.formatTime(is24Hour),
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = DwInk400,
            )
        }

        // Timeline canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DwTintWater)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cy = size.height / 2f

                // Base track
                drawLine(
                    color = DwWater.copy(alpha = 0.25f),
                    start = Offset(0f, cy),
                    end = Offset(size.width, cy),
                    strokeWidth = 2.5.dp.toPx(),
                )

                // Progress track (start → now) when in active hours
                if (isInActiveHours && nowFraction > 0f) {
                    drawLine(
                        color = DwWater,
                        start = Offset(0f, cy),
                        end = Offset(size.width * nowFraction, cy),
                        strokeWidth = 2.5.dp.toPx(),
                    )
                }

                // Drink slot markers
                if (totalMin > 0f) {
                    slots.forEachIndexed { index, slot ->
                        val slotMin = slot.time.hour * 60 + slot.time.minute
                        val slotMinFromStart = if (slotMin >= startMin) {
                            (slotMin - startMin).toFloat()
                        } else {
                            (24 * 60 - startMin + slotMin).toFloat()
                        }
                        val slotFraction = (slotMinFromStart / totalMin).coerceIn(0f, 1f)
                        val x = slotFraction * size.width

                        val isEdge = index == 0 || index == slots.lastIndex
                        val isPast = isInActiveHours && slotMin <= nowMin

                        drawCircle(
                            color = when {
                                isEdge -> DwWaterDeep
                                isPast -> DwWater.copy(alpha = 0.45f)
                                else -> DwMagenta
                            },
                            radius = if (isEdge) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, cy),
                        )
                    }
                }

                // "Now" indicator — vertical line + downward triangle
                if (isInActiveHours) {
                    val nowX = nowFraction * size.width

                    drawLine(
                        color = DwMagenta.copy(alpha = 0.7f),
                        start = Offset(nowX, 0f),
                        end = Offset(nowX, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                    )

                    val ts = 4.dp.toPx()
                    val trianglePath = Path().apply {
                        moveTo(nowX, ts * 2f)
                        lineTo(nowX - ts, 0f)
                        lineTo(nowX + ts, 0f)
                        close()
                    }
                    drawPath(path = trianglePath, color = DwMagenta, style = Fill)
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScheduleTimelinePreview() {
    DrinkWaterAppTheme {
        HomeScheduleTimeline(
            schedule = drinkScheduleMock,
            startTime = LocalDateTime(2026, 5, 10, 8, 0),
            endTime = LocalDateTime(2026, 5, 10, 20, 0),
            now = LocalDateTime(2026, 5, 10, 13, 15),
            isInActiveHours = true,
        )
    }
}

internal val drinkScheduleMock = DrinkSchedule(
    slots = listOf(
        DrinkSlot(
            time = TimeResult(hour = 0, minute = 7, second = 0, millisecond = 0),
            amountMl = 100f,
        ),
        DrinkSlot(
            time = TimeResult(hour = 0, minute = 9, second = 0, millisecond = 0),
            amountMl = 100f,
        ),
        DrinkSlot(
            time = TimeResult(hour = 0, minute = 10, second = 0, millisecond = 0),
            amountMl = 100f,
        ),
        DrinkSlot(
            time = TimeResult(hour = 0, minute = 14, second = 0, millisecond = 0),
            amountMl = 100f,
        ),
        DrinkSlot(
            time = TimeResult(hour = 0, minute = 20, second = 0, millisecond = 0),
            amountMl = 100f,
        ),
    ),
    actualInterval = TimeResult(hour = 0, minute = 45, second = 0, millisecond = 0),
    count = 5,
)
