package com.eahm.drinkwaterapp.feature.dev.alarmcalculator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.calculator.calculateNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.drinkalarm.toTimeResult
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.pad
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.dev.ui.ScheduleVisualization
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─── Schedule mode ────────────────────────────────────────────────────────────

internal enum class ScheduleMode(val label: String) {
    BY_AMOUNT("By Amount"),
    BY_INTERVAL("By Interval"),
}

private val calculator = DrinkAlarmCalculator2()

@OptIn(ExperimentalTime::class)
@Composable
internal fun AlarmCalculatorScreen(
    startTime: DayTime,
    endTime: DayTime,
    initialGoalMl: Float,
    interval: AlarmInterval,
    onBackClicked: () -> Unit,
) {
    BackHandler(onBack = onBackClicked)

    var startTime by remember { mutableStateOf(startTime) }
    var endTime by remember { mutableStateOf(endTime) }
    var goalAmountMl by remember { mutableFloatStateOf(initialGoalMl) }
    var mode by remember { mutableStateOf(ScheduleMode.BY_INTERVAL) }
    var amountPerDrink by remember { mutableFloatStateOf(250f) }
    var desiredIntervalMin by remember {
        mutableIntStateOf(
            interval.minute.coerceIn(
                15,
                240
            )
        )
    }

    // Recomputes whenever any input changes — no side effects needed.
    val schedule =
        remember(startTime, endTime, goalAmountMl, mode, amountPerDrink, desiredIntervalMin) {
            runCatching {
                when (mode) {
                    ScheduleMode.BY_AMOUNT -> calculator.scheduleByAmount(
                        startTime = startTime,
                        endTime = endTime,
                        goalAmountMl = goalAmountMl,
                        amountPerDrinkMl = amountPerDrink,
                    )

                    ScheduleMode.BY_INTERVAL -> calculator.scheduleByInterval(
                        startTime = startTime.toTimeResult(),
                        endTime = endTime.toTimeResult(),
                        goalAmountMl = goalAmountMl,
                        desiredIntervalMinutes = desiredIntervalMin,
                    )
                }
            }.getOrNull()
        }

    val nextAlarmTime: LocalDateTime? =
        remember(startTime, endTime, goalAmountMl, desiredIntervalMin, mode) {
            if (mode == ScheduleMode.BY_INTERVAL) {
                runCatching {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    calculateNextAlarmSlot(
                        now = now,
                        startTime = startTime.toTimeResult(),
                        endTime = endTime.toTimeResult(),
                        goalMl = goalAmountMl,
                        intervalMinutes = desiredIntervalMin,
                    )
                }.getOrNull()
            } else null
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        TextButton(onClick = onBackClicked) {
            Text("← Back to Menu")
        }

        Text(
            text = "Alarm Calculator",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )

        // ── Time range ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTimeSelectorButton(
                text = "Start  ${startTime.hour.pad()}:${startTime.minute.pad()}",
                time = TimeSelected(startTime.hour, startTime.minute),
                onTimeSelected = { startTime = DayTime(it.hour, it.minute) },
            )
            AppTimeSelectorButton(
                text = "End  ${endTime.hour.pad()}:${endTime.minute.pad()}",
                time = TimeSelected(endTime.hour, endTime.minute),
                onTimeSelected = { endTime = DayTime(it.hour, it.minute) },
            )
        }

        // ── Goal amount ───────────────────────────────────────────────────────
        LabeledSlider(
            label = "Goal amount",
            value = goalAmountMl,
            onValueChange = { goalAmountMl = it.roundToInt().toFloat() },
            valueRange = 500f..4_000f,
            steps = 34,
            unit = "mL",
            accentColor = DwWater,
            tintColor = DwTintWater,
        )

        // ── Mode toggle ───────────────────────────────────────────────────────
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ScheduleMode.entries.forEachIndexed { idx, m ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { mode = m },
                    shape = SegmentedButtonDefaults.itemShape(idx, ScheduleMode.entries.size),
                    label = { Text(m.label, fontFamily = PlusJakartaSans) },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = DwTintWater,
                        activeContentColor = DwWaterDeep,
                    ),
                )
            }
        }

        // ── Mode-specific control ─────────────────────────────────────────────
        when (mode) {
            ScheduleMode.BY_AMOUNT -> LabeledSlider(
                label = "Amount per drink",
                value = amountPerDrink,
                onValueChange = { amountPerDrink = it },
                valueRange = 50f..500f,
                steps = 17,
                unit = "mL",
                accentColor = DwMagenta,
                tintColor = DwTintMagenta,
            )

            ScheduleMode.BY_INTERVAL -> {
                val h = desiredIntervalMin / 60
                val m = desiredIntervalMin % 60
                val formatted = buildString {
                    if (h > 0) append("${h}h ")
                    if (m > 0 || h == 0) append("${m}m")
                }
                LabeledSlider(
                    label = "Desired interval",
                    value = desiredIntervalMin.toFloat(),
                    onValueChange = { desiredIntervalMin = it.roundToInt() },
                    valueRange = 15f..240f,
                    steps = 14,
                    unit = "min",
                    hint = "= $formatted",
                    accentColor = DwMagenta,
                    tintColor = DwTintMagenta,
                )
            }
        }

        HorizontalDivider()

        if (schedule != null) {
            ScheduleVisualization(
                schedule = schedule,
                startTime = startTime,
                endTime = endTime,
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

// ─── LabeledSlider ────────────────────────────────────────────────────────────

/**
 * A slider with a progress bar and an inline editable text field.
 *
 * - Slider and text field are always in sync.
 * - Typing a valid value in range updates the slider immediately.
 * - On focus loss, the text field resets to the current valid value.
 * - An optional [hint] (e.g. "= 2h 0m") is shown below the text field.
 */
@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    unit: String,
    accentColor: Color,
    tintColor: Color,
    hint: String? = null,
    modifier: Modifier = Modifier,
) {
    var textInput by rememberSaveable { mutableStateOf(value.roundToInt().toString()) }
    var isFocused by remember { mutableStateOf(false) }

    // Keep text in sync with slider when user is not typing
    LaunchedEffect(value) {
        if (!isFocused) {
            textInput = value.roundToInt().toString()
        }
    }

    val progressFraction =
        ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
            .coerceIn(0f, 1f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DwInk500,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { new ->
                        val filtered = new.filter { it.isDigit() }.take(5)
                        textInput = filtered
                        val parsed = filtered.toFloatOrNull()
                        if (parsed != null && parsed >= valueRange.start && parsed <= valueRange.endInclusive) {
                            onValueChange(parsed)
                        }
                    },
                    modifier = Modifier
                        .width(104.dp)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                // Reset to current valid value if field was left with invalid input
                                textInput = value.roundToInt().toString()
                            }
                        },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = accentColor,
                        textAlign = TextAlign.End,
                    ),
                    suffix = {
                        Text(
                            text = unit,
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = DwInk500,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                if (hint != null) {
                    Text(
                        text = hint,
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = DwInk500,
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = accentColor,
            trackColor = tintColor,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = tintColor,
            ),
        )
    }
}

@Preview
@Composable
private fun AlarmCalculatorScreenPreview() {
    AlarmCalculatorScreen(
        startTime = DayTime(8, 0),
        endTime = DayTime(22, 0),
        initialGoalMl = 2_000f,
        interval = AlarmInterval(0, 60),
        onBackClicked = {},
    )
}
