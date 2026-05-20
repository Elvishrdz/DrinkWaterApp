package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal const val GOAL_STEP = 100f
internal const val GOAL_MIN = 100f
internal const val GOAL_MAX = 100_000f

internal enum class DisplayUnit(val label: String) { ML("mL"), L("L"), FL_OZ("fl oz") }

internal fun effectiveDisplayUnit(pref: UnitPreference, valueMl: Float): DisplayUnit = when (pref) {
    UnitPreference.FL_OZ -> DisplayUnit.FL_OZ
    UnitPreference.AUTO_LITERS -> if (valueMl >= 1000f) DisplayUnit.L else DisplayUnit.ML
    UnitPreference.ML -> DisplayUnit.ML
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalBottomSheet(
    goalMl: Float,
    onGoalChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var value by remember { mutableFloatStateOf(goalMl.coerceIn(GOAL_MIN, GOAL_MAX)) }

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
                text = stringResource(R.string.label_daily_goal),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(32.dp))

            GoalStepperRow(
                value = value,
                onDecrement = { value -= GOAL_STEP },
                onIncrement = { value += GOAL_STEP },
                onValueSet = { value = it },
                unitPreference = unitPreference,
            )

            Spacer(Modifier.height(16.dp))

            DwPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onGoalChanged(value)
                    dismissWithAnimation()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun GoalStepperRow(
    value: Float,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onValueSet: (Float) -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val displayUnit = effectiveDisplayUnit(unitPreference, value)

    val (minDisplay, maxDisplay) = when (displayUnit) {
        DisplayUnit.ML    -> Pair(GOAL_MIN, GOAL_MAX)
        DisplayUnit.FL_OZ -> Pair(GOAL_MIN / 29.574f, GOAL_MAX / 29.574f)
        DisplayUnit.L     -> Pair(GOAL_MIN / 1000f, GOAL_MAX / 1000f)
    }

    fun formatBound(v: Float) = if (displayUnit == DisplayUnit.ML) v.toInt().toString() else "%.1f".format(v)

    fun trySave() {
        val parsed = textInput.trim().toFloatOrNull()
        when {
            parsed == null ->
                errorMessage = context.getString(R.string.validation_error_invalid_number)
            parsed < minDisplay ->
                errorMessage = context.getString(R.string.validation_error_min_value, formatBound(minDisplay))
            parsed > maxDisplay ->
                errorMessage = context.getString(R.string.validation_error_max_value, formatBound(maxDisplay))
            else -> {
                val normalisedDisplay = when (displayUnit) {
                    DisplayUnit.ML    -> parsed.roundToInt().toFloat()
                    DisplayUnit.FL_OZ -> (parsed * 10).roundToInt() / 10f
                    DisplayUnit.L     -> (parsed * 10).roundToInt() / 10f
                }
                val amountMl = when (displayUnit) {
                    DisplayUnit.ML    -> normalisedDisplay
                    DisplayUnit.FL_OZ -> normalisedDisplay * 29.574f
                    DisplayUnit.L     -> normalisedDisplay * 1000f
                }
                onValueSet(amountMl)
                isEditing = false
                textInput = ""
                errorMessage = ""
                focusManager.clearFocus()
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = isEditing,
            label = "goal_stepper_edit",
        ) { editing ->
            if (!editing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StepperCircleButton(
                        enabled = value > GOAL_MIN,
                        onClick = onDecrement,
                        icon = { MinusLine() },
                    )

                    Spacer(Modifier.width(28.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = value.formatVolume(unitPreference),
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.clickable { isEditing = true },
                        )
                    }

                    Spacer(Modifier.width(28.dp))

                    StepperCircleButton(
                        enabled = value < GOAL_MAX,
                        onClick = onIncrement,
                        icon = {
                            Icon(
                                imageVector = DwIcons.Plus,
                                contentDescription = "Increase",
                                tint = DwWater,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it; errorMessage = "" },
                        label = { Text("${stringResource(R.string.settings_stepper_custom_hint)} (${displayUnit.label})") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty(),
                        supportingText = if (errorMessage.isNotEmpty()) {
                            { Text(errorMessage) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { trySave() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = {
                            isEditing = false
                            textInput = ""
                            errorMessage = ""
                            focusManager.clearFocus()
                        }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                        TextButton(onClick = { trySave() }) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StepperCircleButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        icon()
    }
}

/** A horizontal pill used as the minus indicator inside a stepper button. */
@Composable
private fun MinusLine() {
    Box(
        modifier = Modifier
            .size(width = 16.dp, height = 2.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DwWater),
    )
}
