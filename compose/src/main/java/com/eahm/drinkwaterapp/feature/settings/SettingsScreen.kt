package com.eahm.drinkwaterapp.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.settings.ui.EditButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorDialog
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import org.koin.compose.viewmodel.koinViewModel

private val gradientMagentaHero = Brush.verticalGradient(
    colors = listOf(Color(0xFFB83A95), DwMagentaDark)
)

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsContent(
        state = state,
        onNotificationsToggled = viewModel::onNotificationsToggled,
        onUsernameChanged = viewModel::onUsernameChanged,
        onGoalTap = {},
        onUnitsTap = {},
        onIntervalTap = {},
        onStartTimeChanged = viewModel::onStartTimeChanged,
        onEndTimeChanged = viewModel::onEndTimeChanged,
        onShareTap = {},
        onRateTap = {},
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    onNotificationsToggled: (Boolean) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onGoalTap: () -> Unit,
    onUnitsTap: () -> Unit,
    onIntervalTap: () -> Unit,
    onStartTimeChanged: (TimeSelected) -> Unit,
    onEndTimeChanged: (TimeSelected) -> Unit,
    onShareTap: () -> Unit,
    onRateTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DwInk50),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Settings",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = DwInk900,
            )
        }

        item {
            ProfileCard(
                state = state,
                onUsernameChanged = onUsernameChanged,
            )
        }

        item {
            SettingsSection(title = "HYDRATION") {
                SettingRow(
                    icon = DwIcons.Drop,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = "Daily goal",
                    onClick = onGoalTap,
                ) {
                    RowValueTrailing(value = formatGoal(state.dailyGoalMl))
                }
                HorizontalDivider(color = DwInk100)
                SettingRow(
                    icon = DwIcons.Ruler,
                    iconBg = DwTintMagenta,
                    iconTint = DwMagenta,
                    label = "Units",
                    onClick = onUnitsTap,
                ) {
                    RowValueTrailing(value = "${state.unit.label} · kg")
                }
            }
        }

        item {
            SettingsSection(title = "REMINDERS") {
                SettingRow(
                    icon = DwIcons.Bell,
                    iconBg = DwTintMagenta,
                    iconTint = DwMagenta,
                    label = "Notifications",
                ) {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = onNotificationsToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DwPaper,
                            checkedTrackColor = DwMagenta,
                            uncheckedThumbColor = DwPaper,
                            uncheckedTrackColor = DwInk200,
                            uncheckedBorderColor = DwInk200,
                        ),
                    )
                }
                HorizontalDivider(color = DwInk100)
                SettingRow(
                    icon = DwIcons.Clock,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = "Interval",
                    onClick = onIntervalTap,
                ) {
                    RowValueTrailing(value = "every ${state.intervalMinutes} min")
                }
                HorizontalDivider(color = DwInk100)
                SettingRowTimeSelector(
                    time = state.startTime?.let {
                        TimeSelected(
                            hour = it.hour,
                            minute = it.minute,
                        )
                    },
                    icon = DwIcons.Sun,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = "Start time",
                    onStartTimeTap = onStartTimeChanged,
                )
                HorizontalDivider(color = DwInk100)
                SettingRowTimeSelector(
                    time = state.endTime?.let {
                        TimeSelected(
                            hour = it.hour,
                            minute = it.minute,
                        )
                    },
                    icon = DwIcons.Moon,
                    iconBg = DwTintMagenta,
                    iconTint = DwMagenta,
                    label = "End time",
                    onStartTimeTap = onEndTimeChanged,
                )
            }
        }

        item {
            SettingsSection(title = "ABOUT") {
                SettingRow(
                    icon = DwIcons.Share,
                    iconBg = DwInk100,
                    iconTint = DwInk500,
                    label = "Share with friends",
                    onClick = onShareTap,
                ) {
                    RowChevronTrailing()
                }
                HorizontalDivider(color = DwInk100)
                SettingRow(
                    icon = DwIcons.Star,
                    iconBg = DwInk100,
                    iconTint = DwInk500,
                    label = "Rate the app",
                    onClick = onRateTap,
                ) {
                    RowChevronTrailing()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingRowTimeSelector(
    time: TimeSelected?,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier,
    is24HourFormat: Boolean = rememberTimePickerState().is24hour,
    onStartTimeTap: (TimeSelected) -> Unit,
) {
    var showModal by rememberSaveable { mutableStateOf(false) }

    SettingRow(
        modifier = modifier,
        icon = icon,
        iconBg = iconBg,
        iconTint = iconTint,
        label = label,
        onClick = { showModal = !showModal },
    ) {
        RowValueTrailing(
            value = time?.let { formatTime(it.hour, it.minute) }.orEmpty(),
        )
    }

    if (showModal) {
        AppTimeSelectorDialog(
            time = time,
            is24HourFormat = is24HourFormat,
            onTimeSelected = onStartTimeTap,
            onDismiss = { showModal = false },
        )
    }
}

@Composable
private fun ProfileCard(
    state: SettingsState,
    onUsernameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(gradientMagentaHero)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
        ) {
            state.username?.let {
                Text(
                    text = it.take(1).uppercase(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
            }
                ?: Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_drink_holo_dark),
                    contentDescription = null,
                )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.username ?: "Good morning",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${state.weightKg.toInt()} kg · daily goal ${formatGoal(state.dailyGoalMl)}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }

        EditButton(
            username = state.username,
            onUsernameChanged = onUsernameChanged,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = DwInk500,
            letterSpacing = TextUnit(0.10f, TextUnitType.Em),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(modifier = Modifier.outlineBorderStyle()) {
            content()
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = DwInk900,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun RowValueTrailing(value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = DwInk500,
        )
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun RowChevronTrailing() {
    Icon(
        imageVector = DwIcons.ChevronRight,
        contentDescription = null,
        tint = DwInk400,
        modifier = Modifier.size(16.dp),
    )
}

private fun formatGoal(ml: Int): String {
    if (ml < 1000) return "$ml mL"
    val litres = ml / 1000f
    return if (litres % 1f == 0f) "${litres.toInt()} L" else "${"%.1f".format(litres)} L"
}

private fun formatTime(hour: Int, minute: Int): String {
    val ampm = if (hour < 12) "a.m." else "p.m."
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $ampm"
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun SettingsScreenPreview() {
    DrinkWaterAppTheme {
        SettingsContent(
            state = settingsStateMock,
            onNotificationsToggled = {},
            onUsernameChanged = {},
            onGoalTap = {},
            onUnitsTap = {},
            onIntervalTap = {},
            onStartTimeChanged = {},
            onEndTimeChanged = {},
            onShareTap = {},
            onRateTap = {},
        )
    }
}

private val settingsStateMock = SettingsState(
    username = null,
    startTime = null,
    endTime = null,
)
