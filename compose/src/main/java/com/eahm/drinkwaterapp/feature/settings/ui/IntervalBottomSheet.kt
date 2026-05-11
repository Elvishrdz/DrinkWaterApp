package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWarning
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch

private const val INTERVAL_STEP = 5
private const val INTERVAL_MIN = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IntervalBottomSheet(
    currentMinutes: Int,
    /** Duration of the active window (startTime → endTime) in minutes. */
    windowMinutes: Int,
    onIntervalChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxMinutes = (windowMinutes - INTERVAL_STEP).coerceAtLeast(INTERVAL_MIN)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var value by remember {
        mutableIntStateOf(currentMinutes.coerceIn(INTERVAL_MIN, maxMinutes))
    }

    val isAtMax = value >= maxMinutes
    val isAtMin = value <= INTERVAL_MIN

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Reminder interval",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IntervalStepperButton(
                    enabled = !isAtMin,
                    onClick = { value = (value - INTERVAL_STEP).coerceAtLeast(INTERVAL_MIN) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (!isAtMin) DwWater else DwWater.copy(alpha = 0.4f)),
                    )
                }

                Spacer(Modifier.width(28.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatIntervalDisplay(value),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = formatIntervalUnit(value),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = DwInk400,
                    )
                }

                Spacer(Modifier.width(28.dp))

                IntervalStepperButton(
                    enabled = !isAtMax,
                    onClick = { value = (value + INTERVAL_STEP).coerceAtMost(maxMinutes) },
                ) {
                    Icon(
                        imageVector = DwIcons.Plus,
                        contentDescription = "Increase",
                        tint = if (!isAtMax) DwWater else DwWater.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Active window: ${formatWindowHint(windowMinutes)} · Max: ${maxMinutes}m",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = if (isAtMax) DwWarning else DwInk500,
            )

            Spacer(Modifier.height(32.dp))

            DwPrimaryButton(
                text = "Save",
                onClick = {
                    onIntervalChanged(value)
                    dismissWithAnimation()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun IntervalStepperButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        content()
    }
}

/** Compact display value: "45", "1h", "1h 30" */
private fun formatIntervalDisplay(minutes: Int): String = when {
    minutes < 60 -> "$minutes"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}"
}

/** Unit label below the large number */
private fun formatIntervalUnit(minutes: Int): String = when {
    minutes < 60 -> "minutes"
    minutes == 60 -> "hour"
    minutes % 60 == 0 -> "hours"
    else -> "hr"
}

/** Human-readable window duration for the hint row */
private fun formatWindowHint(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}
