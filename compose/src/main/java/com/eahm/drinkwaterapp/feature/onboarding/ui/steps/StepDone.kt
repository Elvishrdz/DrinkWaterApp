package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingFormState
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell
import kotlinx.coroutines.delay

private const val ROW_STAGGER_MS = 150L
private const val ROW_START_DELAY_MS = 150L
private const val BUTTON_DELAY_MS = 900L

@Composable
internal fun StepDone(
    step: Int,
    state: OnboardingFormState,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    val title = if (state.name.isBlank()) {
        stringResource(R.string.onb_done_title)
    } else {
        stringResource(R.string.onb_done_title_with_name, state.name)
    }
    val context = LocalContext.current
    val is24Hour = when (state.timeFormatPreference) {
        TimeFormatPreference.SYSTEM -> DateFormat.is24HourFormat(context)
        TimeFormatPreference.HOUR_12 -> false
        TimeFormatPreference.HOUR_24 -> true
    }

    OnbShell(
        step = step,
        total = step,
        title = title,
        subtitle = stringResource(R.string.onb_done_subtitle),
        onBack = onBack,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_done_cta),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                delayMs = BUTTON_DELAY_MS,
            )
        },
    ) {
        SummaryCard(state = state, is24Hour = is24Hour)
    }
}

@Composable
private fun SummaryCard(
    state: OnboardingFormState,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        SummaryRowData(
            icon = DwIcons.Drop,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.label_daily_goal),
            value = "${state.goalMl.toInt()} mL",
        ),
        SummaryRowData(
            icon = DwIcons.Bell,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = stringResource(R.string.onb_done_row_reminder),
            value = formatIntervalLocalized(state.intervalMinutes),
        ),
        SummaryRowData(
            icon = DwIcons.Sun,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.label_start_time),
            value = state.wakeTime.formatTime(is24Hour),
        ),
        SummaryRowData(
            icon = DwIcons.Moon,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = stringResource(R.string.label_end_time),
            value = state.sleepTime.formatTime(is24Hour),
        ),
    )

    val isPreview = LocalInspectionMode.current
    val rowVisible = remember { List(rows.size) { mutableStateOf(isPreview) } }

    LaunchedEffect(Unit) {
        rows.indices.forEach { index ->
            delay(ROW_START_DELAY_MS + ROW_STAGGER_MS * index)
            rowVisible[index].value = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        rows.forEachIndexed { index, row ->
            AnimatedVisibility(
                visible = rowVisible[index].value,
                enter = fadeIn(animationSpec = tween(250)) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetY = { -it },
                ),
            ) {
                Column {
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    SummaryRow(data = row)
                }
            }
        }
    }
}

private data class SummaryRowData(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val label: String,
    val value: String,
)

@Composable
private fun SummaryRow(
    data: SummaryRowData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(data.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(0.dp))
        Text(
            text = data.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = data.value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun formatIntervalLocalized(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> stringResource(R.string.onb_done_interval_min, m)
        m == 0 -> stringResource(R.string.onb_done_interval_hour, h)
        else -> stringResource(R.string.onb_done_interval_hour_min, h, m)
    }
}

private fun OnbTimeSelection.formatTime(is24Hour: Boolean): String {
    val mm = minute.toString().padStart(2, '0')
    return if (is24Hour) {
        "${toHour24().toString().padStart(2, '0')}:$mm"
    } else {
        "$hour:$mm ${if (isPm) "PM" else "AM"}"
    }
}

@DwPreview
@Composable
private fun StepDonePreview() {
    DrinkWaterAppTheme {
        StepDone(
            step = 1,
            state = OnboardingFormState(
                name = "Alex",
                intervalMinutes = 45,
                goalMl = 2200f,
            ),
            onStart = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepDoneNoNamePreview() {
    DrinkWaterAppTheme {
        StepDone(
            step = 1,
            state = OnboardingFormState(
                name = "",
                intervalMinutes = 45,
                goalMl = 2200f,
            ),
            onStart = {},
            onBack = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepDoneLandscapePreview() {
    DrinkWaterAppTheme {
        StepDone(
            step = 9,
            state = OnboardingFormState(name = "Alex", intervalMinutes = 45, goalMl = 2200f),
            onStart = {},
            onBack = {},
        )
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepDoneTabletPreview() {
    DrinkWaterAppTheme {
        StepDone(
            step = 9,
            state = OnboardingFormState(name = "Alex", intervalMinutes = 45, goalMl = 2200f),
            onStart = {},
            onBack = {},
        )
    }
}
