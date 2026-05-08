package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.calculator.calculateNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.ScheduleMode
import com.eahm.drinkwaterapp.feature.dev.ui.ScheduleVisualization
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmIntervalsScreen(
    state: AlarmIntervalsState,
    onAction: (AlarmIntervalsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intervals") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text("← Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is AlarmIntervalsState.Loading -> CircularProgressIndicator()
                is AlarmIntervalsState.Data -> Content(state)
            }
        }
    }
}

private val calculator = DrinkAlarmCalculator2()

@OptIn(ExperimentalTime::class)
@Composable
private fun Content(state: AlarmIntervalsState.Data) {
    Column {
        Text("Intervals playground")
        Text("Time: ${state.now}")
        Text("Start time: ${state.startTime}")
        Text("End time: ${state.endTime}")
        Text("Interval: ${state.interval}")
        Text("Interval (minutes): ${state.intervalMinutes}")
        Text("Goal (Ml): ${state.goalMl}")
        Text("What to do: ${state.whatToDoResult}")

        val mode = ScheduleMode.BY_INTERVAL
        val start = DayTime(state.startTime.hour, state.startTime.minute)
        val end = DayTime(state.endTime.hour, state.endTime.minute)

        val schedule = remember(state.startTime, state.endTime, state.goalMl) {
            runCatching {
                when (mode) {
                    ScheduleMode.BY_AMOUNT -> calculator.scheduleByAmount(
                        startTime = start,
                        endTime = end,
                        goalAmountMl = state.goalMl,
                        amountPerDrinkMl = 200f,
                    )

                    ScheduleMode.BY_INTERVAL -> calculator.scheduleByInterval(
                        startTime = start,
                        endTime = end,
                        goalAmountMl = state.goalMl,
                        desiredIntervalMinutes = state.intervalMinutes,
                    )
                }
            }.getOrNull()
        }

        val nextAlarmTime: LocalDateTime? =
            remember(state.startTime, state.endTime, state.goalMl) {
                if (mode == ScheduleMode.BY_INTERVAL) {
                    runCatching {
                        val now =
                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        calculateNextAlarmSlot(
                            now = now,
                            startTime = start,
                            endTime = end,
                            goalMl = state.goalMl,
                            intervalMinutes = state.intervalMinutes,
                        )

                    }.getOrNull()
                } else null
            }

        if (schedule != null) {
            ScheduleVisualization(
                schedule = schedule,
                startTime = DayTime(state.startTime.hour, state.startTime.minute),
                endTime = DayTime(state.endTime.hour, state.endTime.minute),
                nextAlarmTime = nextAlarmTime,
            )
        } else {
            Text(
                text = "Invalid configuration — make sure start time is before end time.",
                color = DwMagenta,
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
            )
        }
    }
}

@Preview
@Composable
private fun AlarmIntervalsScreenPreview() {
    AlarmIntervalsScreen(
        state = alarmIntervalsStateMock,
        onAction = {},
        onBackClicked = {},
    )
}

private val alarmIntervalsStateMock = AlarmIntervalsState.Data(
    now = LocalDateTime(2026, 5, 8, 9, 19),
    startTime = LocalDateTime(2026, 5, 8, 9, 19),
    endTime = LocalDateTime(2026, 5, 8, 9, 19),
    interval = AlarmInterval(0, 5),
    intervalMinutes = 5,
    goalMl = 100f,
    whatToDoResult = WhatToDoNow.DoNothing,
)
