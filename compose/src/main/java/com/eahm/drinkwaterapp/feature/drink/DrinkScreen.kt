package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.ui.ProgressRing
import com.eahm.drinkwaterapp.feature.drink.ui.QuickAddRow
import com.eahm.drinkwaterapp.feature.drink.ui.TodayLogSection
import com.eahm.drinkwaterapp.feature.drink.ui.previewLogEntries
import com.eahm.drinkwaterapp.ui.inputfield.AmountInputField
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import kotlinx.datetime.LocalDateTime

@Composable
internal fun DrinkScreen(
    state: DrinkState.Data,
    onDrink: (Float) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onStopAlarm: () -> Unit,
    onGoalAmountChanged: (String) -> Unit,
    getRemainingAlarmTime: () -> String,
    onAlarmIntervalChange: (AlarmInterval) -> Unit,
    onCustom: () -> Unit = {},
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = DwInk50,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Spacer(Modifier.height(it.calculateTopPadding()))

            TodayHeader(
                username = state.username,
                streak = state.currentStreak,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ProgressRing(
                    current = state.drinkStats?.drinkAmount ?: 0f,
                    goal = state.drinkStats?.goalAmount ?: 0f,
                )

                Spacer(modifier = Modifier.height(10.dp))

                RemainingPill(
                    remaining = state.drinkStats?.remainingAmount ?: 0f,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            QuickAddRow(
                presets = state.drinkPresets,
                onDrink = onDrink,
                onCustom = onCustom,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TodayLogSection(
                items = state.todayLogs,
                onSeeAll = onSeeAll,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(state.drinkStats != null) {
                state.drinkStats?.let {
                    AmountInputField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Goal amount (mL)",
                        value = it.goalAmount.toDigitString(),
                        onValueSaved = { newAmount ->
                            onGoalAmountChanged(newAmount)
                        },
                    )
                }
            }

            Spacer(Modifier.height(it.calculateBottomPadding()))
        }
    }
}

@Composable
private fun TodayHeader(
    username: String?,
    streak: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {

        Column {
            Text(
                text = "Good morning${if (username != null) "," else ""}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk400
            )

            username?.let {
                Text(
                    text = it,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = (26 * 1.2).sp,
                    color = DwInk900
                )
            }
        }

        StreakBadge(streak = streak)
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    val gradientMagenta = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB83A95),
            DwMagentaDark
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(gradientMagenta)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = DwIcons.Star,
            contentDescription = null,
            tint = DwPaper,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$streak-day streak",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = DwPaper
        )
    }
}

@Composable
private fun RemainingPill(remaining: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DwTintWater)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (remaining > 0) {
            Text(
                text = buildAnnotatedString {
                    append("You're ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = DwWater
                        )
                    ) {
                        append("${remaining.toDigitString()} mL")
                    }
                    append(" away from today's goal")
                },
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                lineHeight = (13 * 1.45).sp
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DwWater)) {
                        append("Goal reached")
                    }
                    append(" — keep sipping to stay ahead")
                },
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400
            )
        }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
private fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onDelete = {},
            onDateChanged = {},
            onStopAlarm = {},
            onGoalAmountChanged = {},
            getRemainingAlarmTime = { "" },
            onAlarmIntervalChange = {},

            onCustom = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DrinkScreenEmptyPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onDelete = {},
            onDateChanged = {},
            onStopAlarm = {},
            onGoalAmountChanged = {},
            getRemainingAlarmTime = { "" },
            onAlarmIntervalChange = {},

            onCustom = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DrinkScreenGoalReachedPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onDelete = {},
            onDateChanged = {},
            onStopAlarm = {},
            onGoalAmountChanged = {},
            getRemainingAlarmTime = { "" },
            onAlarmIntervalChange = {},

            onCustom = {},
            onSeeAll = {}
        )
    }
}

internal val drinkPresetsMock = listOf(
    DrinkPreset(1, 100f),
    DrinkPreset(2, 200f),
    DrinkPreset(3, 300f),
)

private val stateMock = DrinkState.Data(
    username = "Dave",
    currentStreak = 1,
    drinkPresets = drinkPresetsMock,
    date = LocalDateTime(2023, 1, 1, 12, 0),
    historyRecords = emptyList(),
    todayLogs = previewLogEntries,
    drinkStats = DrinkStats(
        goalAmount = 2400f,
        drinkAmount = 1500f,
        goalReached = false,
    ),
    isAlarmActive = false,
    alarmInterval = AlarmInterval(0, 5),
    alarmNextInterval = null,
)
