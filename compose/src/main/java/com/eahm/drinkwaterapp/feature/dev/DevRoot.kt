package com.eahm.drinkwaterapp.feature.dev

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealSoft
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.drinkStatsMock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.Adaptive
import com.eahm.drinkwaterapp.feature.drink.DrinkStats
import com.eahm.drinkwaterapp.ui.drinkbutton.DrinkButton
import com.eahm.drinkwaterapp.ui.historylist.HistoryButton
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.inputfield.AmountInputField
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import com.eahm.drinkwaterapp.ui.timepicker.AppTimePickerButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

enum class DevDestination(
    val label: String,
    val icon: ImageVector,
) {
    MENU("Menu", DwIcons.Drop),
    VALUES("Values", DwIcons.Drop),
    ADAPTIVE("Adaptive", DwIcons.Wave),
}


@Composable
internal fun DevRoot(onBackClicked: () -> Unit) {
    BackHandler(
        onBack = onBackClicked,
    )

    var currentDestination by rememberSaveable { mutableStateOf(DevDestination.MENU) }

    val onBackClicked: () -> Unit = {
        currentDestination = DevDestination.MENU
    }

    when (currentDestination) {
        DevDestination.MENU -> MenuRoot(
            onMenuSelected = { currentDestination = it },
        )

        DevDestination.VALUES -> ValuesRoot(
            onBackClicked = onBackClicked,
        )

        DevDestination.ADAPTIVE -> AdaptiveScreen(
            onBackClicked = onBackClicked,
        )
    }
}

@Composable
private fun MenuRoot(
    onMenuSelected: (DevDestination) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(70.dp),
    ) {
        items(DevDestination.entries.filterNot { it == DevDestination.MENU }) {
            MiniCard(
                icon = it.icon,
                iconBg = DwTealSoft,
                iconTint = DwTealDark,
                value = "",
                label = it.label,
                onClick = { onMenuSelected(it) },
            )
        }
    }
}

@Composable
private fun ValuesRoot(
    onBackClicked: () -> Unit,
    viewModel: DevViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(
        onBack = onBackClicked,
    )

    when (val data = state) {
        DevState.Loading -> {
            /* Idle */
        }

        is DevState.Data -> {
            DevScreen(
                state = data,
                onDrink = viewModel::onDrink,
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
                onStopAlarm = viewModel::onStopAlarm,
                onGoalAmountChanged = viewModel::onGoalChanged,
                getRemainingAlarmTime = viewModel::getRemainingAlarmTime,
                onAlarmIntervalChange = viewModel::onAlarmIntervalChange,
                onBackClicked = onBackClicked,
            )
        }
    }
}

@Composable
internal fun DevScreen(
    state: DevState.Data,
    onDrink: (Float) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onStopAlarm: () -> Unit,
    onGoalAmountChanged: (String) -> Unit,
    getRemainingAlarmTime: () -> String,
    onAlarmIntervalChange: (AlarmInterval) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Content(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            date = state.date,
            historyRecords = state.historyRecords,
            drinkStats = state.drinkStats,
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
            onBackClicked = onBackClicked,
        )
    }
}

@Composable
private fun Content(
    date: LocalDateTime,
    historyRecords: List<HourGroup>,
    drinkStats: DrinkStats?,
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
    onBackClicked: () -> Unit,
) {
    var timer by remember { mutableStateOf("") }

    Adaptive(
        modifier = modifier,
        stats = {
            DwPrimaryButton(
                text = "Back to Menu",
                onClick = onBackClicked,
            )

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

            AnimatedVisibility(drinkStats != null) {
                drinkStats?.let {
                    Column {
                        Text(
                            text = "Goal: ${it.goalAmount}",
                        )

                        Text(
                            text = "Current Drink Amount: ${it.drinkAmount}",
                        )

                        Text(
                            text = "Goal reached: ${it.goalReached}",
                        )
                    }
                }
            }

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

            AnimatedVisibility(drinkStats != null) {
                drinkStats?.let {
                    AmountInputField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Goal amount (mL)",
                        value = it.drinkAmount.toDigitString(),
                        onValueSaved = { newAmount ->
                            onGoalAmountChanged(newAmount)
                        },
                    )
                }
            }
        },
        drink = {
            DrinkButton(
                text = "Drink Water",
                isGoalReached = drinkStats?.goalReached == true,
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

@Preview(showBackground = true)
@Composable
private fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        Content(
            date = Mock.localDateTimeMock,
            historyRecords = Mock.hourGroupMock,
            drinkStats = Mock.drinkStatsMock,
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
            onBackClicked = {},
        )
    }
}

