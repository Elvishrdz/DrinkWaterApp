package com.eahm.drinkwaterapp.feature.dev.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.calculateTimeDifference
import com.eahm.drinkwaterapp.core.data.drinkalarm.inWholeMinutes
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.pad
import kotlinx.datetime.LocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ScheduleVisualization(
    schedule: DrinkSchedule,
    startTime: DayTime,
    endTime: DayTime,
    nextAlarmTime: LocalDateTime? = null,
) {
    // Day coverage = active window / 24 h
    val windowMinutes = DayTime.calculateTimeDifference(startTime, endTime).inWholeMinutes
    val dayCoverageFraction = (windowMinutes / (24 * 60).toFloat()).coerceIn(0f, 1f)
    val dayCoveragePct = (dayCoverageFraction * 100).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Summary chips ─────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryChip(label = "Drinks", value = "${schedule.count}")

            val iv = schedule.actualInterval
            val intervalLabel = buildString {
                if (iv.hour > 0) append("${iv.hour}h ")
                if (iv.minute > 0 || iv.hour == 0) append("${iv.minute}m")
            }
            SummaryChip(label = "Interval", value = intervalLabel)

            val base = schedule.slots.firstOrNull()?.amountMl?.roundToInt() ?: 0
            SummaryChip(label = "Per drink", value = "${base}mL")

            nextAlarmTime?.let { time ->
                SummaryChip(
                    label = "Next alarm",
                    value = "${time.hour.pad()}:${time.minute.pad()}",
                )
            }
        }

        // ── Timeline chart ────────────────────────────────────────────────────
        if (schedule.slots.size >= 2) {
            TimelineChart(schedule = schedule)
        }

        // ── Day coverage progress ─────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Day coverage",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = DwInk500,
                )
                Text(
                    text = "$dayCoveragePct% of 24 h",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DwWaterDeep,
                )
            }
            LinearProgressIndicator(
                progress = { dayCoverageFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = DwWater,
                trackColor = DwTintWater,
            )
        }

        ScheduleSlotChips(schedule)
    }
}


@Composable
private fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(DwTintWater, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = DwWaterDeep,
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            color = DwInk500,
        )
    }
}

@Composable
private fun TimelineChart(schedule: DrinkSchedule) {
    val slots = schedule.slots
    val firstTime = slots.first().time
    val lastTime = slots.last().time
    val totalMin = DayTime.calculateTimeDifference(firstTime, lastTime)
        .inWholeMinutes.toFloat().coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(DwTintWater, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height / 2f

            // Timeline bar
            drawLine(
                color = DwWater,
                start = Offset(0f, cy),
                end = Offset(size.width, cy),
                strokeWidth = 3.dp.toPx(),
            )

            // Drink markers
            slots.forEachIndexed { index, slot ->
                val minFromStart = DayTime.calculateTimeDifference(firstTime, slot.time)
                    .inWholeMinutes.toFloat()
                val x = (minFromStart / totalMin) * size.width
                val isEdge = index == 0 || index == slots.lastIndex

                drawCircle(
                    color = if (isEdge) DwWaterDeep else DwMagenta,
                    radius = if (isEdge) 7.dp.toPx() else 4.dp.toPx(),
                    center = Offset(x, cy),
                )
            }
        }
    }
}
