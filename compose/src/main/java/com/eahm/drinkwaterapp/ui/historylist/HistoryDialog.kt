package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime
import java.util.Locale
import kotlin.time.ExperimentalTime

data class HistoryUI(
    val id: String,
    val amount: String,
    val status: Int,
    val timestamp: LocalDateTime,
)

data class DayGroup(
    val hour: Int,
    val day: Int,
    val month: Int,
    val year: Int,
)

@Immutable
data class HourGroup(
    val hour: Int,
    val isCurrentHour: Boolean,
    val items: List<HistoryUI>
)

private val iconBoxShape = RoundedCornerShape(9.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    date: LocalDateTime,
    historyRecords: ImmutableList<HourGroup>,
    onDelete: (HistoryUI) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        ),
    ) {
        ElevatedCard(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    text = stringResource(R.string.history_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DwInk900,
                )

                DateTimePickerButton(
                    selectedDateTime = date,
                    onDateTimeSelected = onDateChanged,
                    label = stringResource(R.string.history_select_date_label),
                )

                HistoryTimeline(
                    historyRecords = historyRecords,
                    onDelete = onDelete,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { onDismiss() }
                    ) {
                        Text(text = stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HistoryDialogPreview() {
    HistoryDialog(
        date = Mock.localDateTimeMock,
        historyRecords = Mock.hourGroupMock.toImmutableList(),
        onDismiss = {},
        onDelete = {},
        onDateChanged = {},
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun HistoryTimeline(
    historyRecords: ImmutableList<HourGroup>,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true,
    unitPreference: UnitPreference = UnitPreference.ML,
    // Rendered right after the first group (second list position). Null = no injection.
    secondItemContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 18.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        historyRecords.forEachIndexed { index, hourGroup ->
            TimelineHourGroup(
                hourGroup = hourGroup,
                isCurrentHour = hourGroup.isCurrentHour,
                is24Hour = is24Hour,
                unitPreference = unitPreference,
                onDelete = onDelete,
            )
            if (index == 0 && secondItemContent != null) {
                secondItemContent()
            }
        }
    }
}

@Composable
private fun TimelineHourGroup(
    hourGroup: HourGroup,
    isCurrentHour: Boolean,
    is24Hour: Boolean,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    val hourHeaderText = if (is24Hour) {
        String.format(Locale.getDefault(), "%02d:00", hourGroup.hour)
    } else {
        val h12 = when {
            hourGroup.hour == 0 -> 12; hourGroup.hour > 12 -> hourGroup.hour - 12; else -> hourGroup.hour
        }
        val period = if (hourGroup.hour < 12) "AM" else "PM"
        "$h12 $period"
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Section header — JetBrainsMono label matching SettingsSection style
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Text(
                text = hourHeaderText,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isCurrentHour) DwWater else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = TextUnit(0.10f, TextUnitType.Em),
            )
            if (isCurrentHour) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = stringResource(R.string.history_timeline_now),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DwWater,
                    letterSpacing = TextUnit(0.10f, TextUnitType.Em),
                )
            }
        }

        // Items grouped in an outline-bordered card, separated by dividers
        Column(
            modifier = Modifier
                .outlineBorderStyle()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            hourGroup.items.forEachIndexed { index, item ->
                TimelineItemCard(
                    item = item,
                    is24Hour = is24Hour,
                    unitPreference = unitPreference,
                    onDelete = onDelete
                )
                if (index < hourGroup.items.lastIndex) {
                    HorizontalDivider(color = DwInk100)
                }
            }
        }
    }
}

@Composable
private fun TimelineItemCard(
    item: HistoryUI,
    is24Hour: Boolean,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(iconBoxShape)
                .background(DwTintWater),
        ) {
            Icon(
                painter = painterResource(getIconResId(item.timestamp)),
                contentDescription = null,
                tint = DwWater,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.amount.toFloat().formatVolume(unitPreference),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = formatTime(item.timestamp, is24Hour),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = DwInk400,
            )
        }

        IconButton(
            onClick = { onDelete(item) },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = DwIcons.Trash,
                contentDescription = stringResource(R.string.history_action_delete),
                tint = DwInk300,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun formatTime(timestamp: LocalDateTime, is24Hour: Boolean): String {
    val h = timestamp.hour
    val m = timestamp.minute
    return if (is24Hour) {
        String.format(Locale.getDefault(), "%02d:%02d", h, m)
    } else {
        val h12 = when {
            h == 0 -> 12; h > 12 -> h - 12; else -> h
        }
        val period = if (h < 12) "AM" else "PM"
        String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, period)
    }
}

@OptIn(ExperimentalTime::class)
fun getIconResId(timestamp: LocalDateTime): Int {
    val hour = timestamp.hour  // 0-23
    val secondsSinceMidnight = hour * 3600

    val startMadrugada = 1 * 3600      // 1:00 AM
    val startManana = 5 * 3600         // 5:00 AM
    val startDay = 10 * 3600           // 10:00 AM
    val startTarde = 15 * 3600         // 3:00 PM
    val startNight = 19 * 3600         // 7:00 PM

    return when (secondsSinceMidnight) {
        in startMadrugada..<startManana -> R.drawable.icon_r_madrugada  // 1-4
        in startManana..<startDay -> R.drawable.icon_r_manana            // 5-9
        in startDay..<startTarde -> R.drawable.icon_r_dia                // 10-14
        in startTarde..<startNight -> R.drawable.icon_r_tarde            // 15-18
        else -> R.drawable.icon_r_noche                                  // 19-23, 0-0
    }
}
