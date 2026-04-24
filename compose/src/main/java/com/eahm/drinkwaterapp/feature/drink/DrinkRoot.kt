package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.ui.drinkbutton.DrinkButton
import com.eahm.drinkwaterapp.ui.historylist.HistoryButton
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.inputfield.AmountInputField
import com.eahm.drinkwaterapp.ui.timepicker.AppTimePickerButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun DrinkRoot() {
    val viewModel: DrinkViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        DrinkState.Loading -> {
            /* Idle */
        }

        is DrinkState.Data -> {
            DrinkScreen(
                state = data,
                onDrink = viewModel::onDrink,
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
                onStopAlarm = viewModel::onStopAlarm,
                onGoalAmountChanged = viewModel::onGoalChanged,
                getRemainingAlarmTime = viewModel::getRemainingAlarmTime,
                onAlarmIntervalChange = viewModel::onAlarmIntervalChange,
            )
        }
    }
}

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
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Content(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            date = state.date,
            historyRecords = state.historyRecords,
            goalAmount = state.goalAmount,
            drinkAmount = state.drinkAmount,
            isAlarmActive = state.isAlarmActive,
            alarmInterval = state.alarmInterval,
            alarmNextInterval = state.alarmNextInterval,
            onDrink = onDrink,
            onDelete = onDelete,
            onDateChanged = onDateChanged,
            onStopAlarm = onStopAlarm,
            onGoalAmountChanged = onGoalAmountChanged,
            getRemainingAlarmTime = getRemainingAlarmTime,
            onAlarmIntervalChange = onAlarmIntervalChange,
        )
    }
}

@Composable
private fun Content(
    date: LocalDateTime,
    historyRecords: List<HourGroup>,
    goalAmount: String,
    drinkAmount: Float,
    isAlarmActive: Boolean,
    alarmInterval: AlarmInterval,
    alarmNextInterval: AlarmInterval?,
    modifier: Modifier = Modifier,
    onDrink: (Float) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onStopAlarm: () -> Unit,
    onGoalAmountChanged: (String) -> Unit,
    getRemainingAlarmTime: () -> String,
    onAlarmIntervalChange: (AlarmInterval) -> Unit,
) {
    var timer by remember { mutableStateOf("") }

    Adaptive(
        modifier = modifier,
        stats = {
            Text(
                text = "Is alarm active: $isAlarmActive",
            )

            Text(
                text = "Alarm interval: ${alarmInterval.hour}:${alarmInterval.minute}",
            )

            alarmNextInterval?.let {
                Text(
                    text = "Alarm next interval: ${it.hour}:${it.minute}",
                )
            }

            Text(
                text = "remaining time: $timer",
            )

            Button(
                onClick = onStopAlarm,
            ) {
                Text(
                    text = "Stop Alarm",
                )
            }

            var selected: TimeSelected? by remember { mutableStateOf(null) }

            Text(
                text = "alarm at $selected",
            )

            Text(
                text = "total drink amount of today: $drinkAmount",
            )
            AppTimeSelectorButton(
                text = "Set Alarm Time",
                time = selected,
                onTimeSelected = { selected = it },
            )

            AppTimePickerButton(
                text = "Set Alarm Interval",
                time = TimeSelected(alarmInterval.hour, alarmInterval.minute),
                onTimeSelected = {
                    val alarmInterval = AlarmInterval(it.hour, it.minute)
                    onAlarmIntervalChange(alarmInterval)
                },
            )

            val scope = rememberCoroutineScope()

            LaunchedEffect(scope) {
                scope.launch {
                    while (true) {
                        delay(1.seconds)
                        timer = getRemainingAlarmTime()
                    }
                }
            }

            AmountInputField(
                modifier = Modifier.fillMaxWidth(),
                label = "Goal amount (mL)",
                value = goalAmount,
                onValueSaved = { newAmount ->
                    onGoalAmountChanged(newAmount)
                },
            )
        },
        drink = {
            DrinkButton(
                text = "Drink Water",
                onClick = onDrink,
            )
        },
        history = {
            HistoryButton(
                text = "History",
                date = date,
                historyRecords = historyRecords,
                onDelete = { onDelete(it.toEntity()) },
                onDateChanged = onDateChanged,
            )
        },
    )
}

@PreviewScreenSizes
@Preview
@Composable
private fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        Content(
            date = Mock.localDateTimeMock,
            historyRecords = Mock.hourGroupMock,
            goalAmount = "1000",
            drinkAmount = 0f,
            isAlarmActive = true,
            alarmInterval = AlarmInterval(0, 5),
            alarmNextInterval = null,
            onDrink = {},
            onDelete = {},
            onDateChanged = {},
            onStopAlarm = {},
            onGoalAmountChanged = {},
            getRemainingAlarmTime = { "" },
            onAlarmIntervalChange = {},
        )
    }
}
