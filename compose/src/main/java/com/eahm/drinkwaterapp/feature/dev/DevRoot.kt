package com.eahm.drinkwaterapp.feature.dev

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTeal
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealSoft
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.drinkStatsMock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.dev.adaptive.AdaptiveScreen
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.AlarmCalculatorRoot
import com.eahm.drinkwaterapp.feature.dev.alarmintervals.AlarmIntervalsRoot
import com.eahm.drinkwaterapp.feature.dev.alarms.AlarmsRoot
import com.eahm.drinkwaterapp.feature.dev.dailygoal.DailyGoalDevRoot
import com.eahm.drinkwaterapp.feature.dev.notifications.NotificationsRoot
import com.eahm.drinkwaterapp.feature.dev.onboarding.OnboardingDevRoot
import com.eahm.drinkwaterapp.feature.dev.streak.StreakDevRoot
import com.eahm.drinkwaterapp.feature.dev.ui.Adaptive
import com.eahm.drinkwaterapp.ui.drinkbutton.DrinkButton
import com.eahm.drinkwaterapp.ui.drinks.BubbleConfig
import com.eahm.drinkwaterapp.ui.drinks.DrinkAppearance
import com.eahm.drinkwaterapp.ui.drinks.DrinkShape
import com.eahm.drinkwaterapp.ui.historylist.HistoryButton
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.inputfield.AmountInputField
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import com.eahm.drinkwaterapp.ui.timepicker.AppTimePickerButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

enum class DevDestination(
    val label: String,
    val icon: ImageVector,
) {
    MENU("Menu", DwIcons.Drop),
    VALUES("Values", DwIcons.Drop),
    ADAPTIVE("Adaptive", DwIcons.Wave),
    ALARM_CALCULATOR("Calculator", DwIcons.Clock),
    ALARM_INTERVALS("Intervals", DwIcons.Clock),
    ALARMS("Alarms", DwIcons.Bell),
    DAILY_GOAL("Daily Goal", DwIcons.Drop),
    STREAK("Streak", DwIcons.Star),
    NOTIFICATION("Notification", DwIcons.Bell),
    ONBOARDING("OnBoarding", DwIcons.Sun),
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

    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = DwTealDark,
            selectedTextColor = DwTeal,
            indicatorColor = DwTintMagenta,
            unselectedIconColor = DwInk400,
            unselectedTextColor = DwInk400,
        )
    )

    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = DwInk200,
        wideNavigationRailColors = WideNavigationRailDefaults.colors(
            containerColor = DwInk200,
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            DevDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                    colors = itemColors,
                )
            }
        }
    ) {
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

            DevDestination.ALARM_CALCULATOR -> AlarmCalculatorRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.ALARM_INTERVALS -> AlarmIntervalsRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.ALARMS -> AlarmsRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.DAILY_GOAL -> DailyGoalDevRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.STREAK -> StreakDevRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.NOTIFICATION -> NotificationsRoot(
                onBackClicked = onBackClicked,
            )

            DevDestination.ONBOARDING -> OnboardingDevRoot(
                onBackClicked = onBackClicked,
            )
        }
    }
}

@Composable
private fun MenuRoot(
    onMenuSelected: (DevDestination) -> Unit
) {
    Scaffold { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                onShowConfetti = viewModel::showConfetti,
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
                onStopAlarm = viewModel::onStopAlarm,
                onGoalAmountChanged = viewModel::onGoalChanged,
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
    onShowConfetti: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onStopAlarm: () -> Unit,
    onGoalAmountChanged: (String) -> Unit,
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
            confettiButtonEnabled = state.confettiButtonEnabled,
            onDrink = onDrink,
            onShowConfetti = onShowConfetti,
            onDelete = onDelete,
            onDateChanged = onDateChanged,
            onStopAlarm = onStopAlarm,
            onGoalAmountChanged = onGoalAmountChanged,
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
    confettiButtonEnabled: Boolean,
    modifier: Modifier = Modifier,
    onShowConfetti: () -> Unit,
    onDrink: (Float) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onStopAlarm: () -> Unit,
    onGoalAmountChanged: (String) -> Unit,
    onAlarmIntervalChange: (AlarmInterval) -> Unit,
    onBackClicked: () -> Unit,
) {
    val context = LocalContext.current

    val notificationsAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    Adaptive(
        modifier = modifier,
        stats = {
            DwPrimaryButton(
                text = "Back to Menu",
                onClick = onBackClicked,
            )

            Text(
                text = "Notification permissions granted: $notificationsAllowed",
            )

            Text(
                text = "Is alarm active: $isAlarmActive",
            )

            Text(
                text = "Alarm interval: ${alarmInterval.hour}:${alarmInterval.minute}",
            )

            Button(
                onClick = onStopAlarm,
            ) {
                Text(
                    text = "Stop Alarm",
                )
            }

            Button(
                onClick = onShowConfetti,
                enabled = confettiButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = "Test Confetti",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
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

            DrinkShape(
                modifier = Modifier
                    .size(
                        width = 90.dp,
                        height = 180.dp,
                    ),
                fillFraction = 1f,
                shape = DrinkShape.Bottle,
                appearance = DrinkAppearance(),
                bubbles = BubbleConfig(),
                onFillChanged = {},
            )

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
            confettiButtonEnabled = false,
            onDrink = {},
            onShowConfetti = {},
            onDelete = {},
            onDateChanged = {},
            onStopAlarm = {},
            onGoalAmountChanged = {},
            onAlarmIntervalChange = {},
            onBackClicked = {},
        )
    }
}

