package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwSuccess
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.dev.ui.ScheduleVisualization
import com.eahm.drinkwaterapp.ui.historylist.DateTimePickerButton
import com.eahm.drinkwaterapp.ui.timepicker.AppTimePickerButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.datetime.LocalDateTime
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
                is AlarmIntervalsState.Data -> Content(state, onAction)
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun Content(state: AlarmIntervalsState.Data, onAction: (AlarmIntervalsAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Intervals playground")
        Spacer(Modifier.height(12.dp))

        FlowRow {
            DateTimePickerButton(
                label = "Set date",
                selectedDateTime = state.time,
                onDateTimeSelected = {
                    val updated = LocalDateTime(
                        year = it.year,
                        month = it.month,
                        day = it.day,
                        hour = it.hour,
                        minute = it.minute,
                    )

                    onAction(AlarmIntervalsAction.SetCustomTime(updated))
                },
            )

            AppTimePickerButton(
                text = "Set time",
                time = TimeSelected(state.time.hour, state.time.minute),
                onTimeSelected = {
                    val updated = LocalDateTime(
                        year = state.time.year,
                        month = state.time.month,
                        day = state.time.day,
                        hour = it.hour,
                        minute = it.minute,
                    )

                    onAction(AlarmIntervalsAction.SetCustomTime(updated))
                },
            )

            DwSecondaryButton(
                text = "Clear custom time",
                onClick = { onAction(AlarmIntervalsAction.ClearCustomTime) },
            )
        }

        Spacer(Modifier.height(12.dp))

        Text("Time: ${state.time}", color = DwSuccess)
        Text(
            text = "Custom time: ${state.customTime}",
            color = if (state.customTime == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Now: ${state.now}",
            color = if (state.customTime == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(7.dp))


        Text("Start time: ${state.startTime}")
        Text("End time: ${state.endTime}")
        Text("Interval: ${state.interval}")
        Text("Interval (minutes): ${state.intervalMinutes}")
        Text("Goal (Ml): ${state.goalMl}")
        Text("What to do: ${state.whatToDoResult}")

        if (state.schedule != null) {
            ScheduleVisualization(
                schedule = state.schedule,
                startTime = DayTime(state.startTime.hour, state.startTime.minute),
                endTime = DayTime(state.endTime.hour, state.endTime.minute),
                nextAlarmTime = state.nextAlarmSlot,
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
    time = LocalDateTime(2026, 5, 8, 9, 19),
    now = LocalDateTime(2026, 5, 8, 9, 19),
    customTime = null,
    startTime = LocalDateTime(2026, 5, 8, 9, 19),
    endTime = LocalDateTime(2026, 5, 8, 9, 19),
    goalMl = 100f,
    interval = AlarmInterval(0, 5),
    intervalMinutes = 5,
    whatToDoResult = WhatToDoNow.DoNothing,
    schedule = null, // TODO: create mock data
    nextAlarmSlot = null, // TODO: create mock data
)
