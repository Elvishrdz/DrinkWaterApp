package com.eahm.drinkwaterapp.feature.drink.alarm

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk700
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwSuccess
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.pad
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeAlarmCard(
    modifier: Modifier = Modifier,
    viewModel: HomeAlarmViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        HomeAlarmState.Loading -> {}
        HomeAlarmState.NoPermission -> NoPermissionCard(modifier)
        HomeAlarmState.NotConfigured -> NotConfiguredCard(modifier)
        is HomeAlarmState.Active -> ActiveScheduleCard(state = s, modifier = modifier)
    }
}

@Composable
private fun NoPermissionCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwTintMagenta)
            .clickable {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DwMagenta.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DwIcons.Bell,
                contentDescription = null,
                tint = DwMagenta,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notifications disabled",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DwInk900,
            )
            Text(
                text = "Enable them to receive your drink reminders on time.",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = DwInk500,
                lineHeight = (12 * 1.5).sp,
            )
        }

        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwMagenta,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun NotConfiguredCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwInk100)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DwInk400.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DwIcons.Clock,
                contentDescription = null,
                tint = DwInk400,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "No alarm schedule",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DwInk700,
            )
            Text(
                text = "Configure your active hours in Settings to get reminders.",
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = DwInk500,
                lineHeight = (12 * 1.5).sp,
            )
        }
    }
}

@Composable
private fun ActiveScheduleCard(
    state: HomeAlarmState.Active,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwPaper)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reminders",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DwInk900,
            )
            ActiveStatusBadge(isActive = state.isInActiveHours)
        }

        // Timeline
        if (state.schedule != null) {
            HomeScheduleTimeline(
                schedule = state.schedule,
                startTime = state.startTime,
                endTime = state.endTime,
                now = state.now,
                isInActiveHours = state.isInActiveHours,
            )
        }

        // Summary chips
        SummaryRow(
            schedule = state.schedule,
            interval = state.interval,
            nextAlarmSlot = state.nextAlarmSlot,
            isInActiveHours = state.isInActiveHours,
        )
    }
}

@Composable
private fun ActiveStatusBadge(isActive: Boolean) {
    val bgColor = if (isActive) DwSuccess.copy(alpha = 0.12f) else DwInk200
    val fgColor = if (isActive) DwSuccess else DwInk400
    val label = if (isActive) "Active hours" else "Resting"
    val icon = if (isActive) DwIcons.Sun else DwIcons.Moon

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fgColor,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = fgColor,
        )
    }
}

@Composable
private fun HomeScheduleTimeline(
    schedule: DrinkSchedule,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    now: LocalDateTime,
    isInActiveHours: Boolean,
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
                text = "${startTime.hour.pad()}:${startTime.minute.pad()}",
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                color = DwInk400,
            )
            Text(
                text = "${endTime.hour.pad()}:${endTime.minute.pad()}",
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

@Composable
private fun SummaryRow(
    schedule: DrinkSchedule?,
    interval: AlarmInterval,
    nextAlarmSlot: LocalDateTime?,
    isInActiveHours: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (schedule != null) {
            SummaryChip(label = "Drinks", value = "${schedule.count}")

            val totalMin = interval.hour * 60 + interval.minute
            val h = totalMin / 60
            val m = totalMin % 60
            val intervalLabel = buildString {
                if (h > 0) append("${h}h ")
                if (m > 0 || h == 0) append("${m}m")
            }.trim()
            SummaryChip(label = "Every", value = intervalLabel)
        }

        nextAlarmSlot?.let { next ->
            val label = if (isInActiveHours) "Next" else "Starts"
            SummaryChip(
                label = label,
                value = "${next.hour.pad()}:${next.minute.pad()}",
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DwTintWater)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = DwWaterDeep,
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp,
            color = DwInk500,
        )
    }
}

@Preview
@Composable
private fun NoPermissionPreview() {
    DrinkWaterAppTheme {
        NoPermissionCard(Modifier.padding(20.dp))
    }
}

@Preview
@Composable
private fun NotConfiguredPreview() {
    DrinkWaterAppTheme {
        NotConfiguredCard(Modifier.padding(20.dp))
    }
}

@Preview
@Composable
private fun ActiveInActiveHoursPreview() {
    DrinkWaterAppTheme {
        ActiveScheduleCard(
            state = HomeAlarmState.Active(
                startTime = LocalDateTime(2026, 5, 10, 8, 0),
                endTime = LocalDateTime(2026, 5, 10, 20, 0),
                interval = AlarmInterval(0, 90),
                schedule = null,
                nextAlarmSlot = LocalDateTime(2026, 5, 10, 14, 30),
                isInActiveHours = true,
                now = LocalDateTime(2026, 5, 10, 13, 15),
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview
@Composable
private fun ActiveRestingPreview() {
    DrinkWaterAppTheme {
        ActiveScheduleCard(
            state = HomeAlarmState.Active(
                startTime = LocalDateTime(2026, 5, 10, 8, 0),
                endTime = LocalDateTime(2026, 5, 10, 20, 0),
                interval = AlarmInterval(1, 30),
                schedule = null,
                nextAlarmSlot = LocalDateTime(2026, 5, 10, 8, 0),
                isInActiveHours = false,
                now = LocalDateTime(2026, 5, 10, 22, 0),
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}
