package com.eahm.drinkwaterapp.feature.history.ui

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
import androidx.compose.runtime.mutableIntStateOf
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
import com.eahm.drinkwaterapp.feature.settings.ui.DisplayUnit
import com.eahm.drinkwaterapp.feature.settings.ui.effectiveDisplayUnit
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val RECORD_AMOUNT_STEP = 50
private const val RECORD_AMOUNT_MIN = 50
private const val RECORD_AMOUNT_MAX = 99999

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordAmountBottomSheet(
    currentAmountMl: Float,
    onAmountChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var value by remember {
        mutableIntStateOf(
            currentAmountMl.toInt().coerceIn(RECORD_AMOUNT_MIN, RECORD_AMOUNT_MAX)
                .let { it - (it % RECORD_AMOUNT_STEP) }
                .coerceAtLeast(RECORD_AMOUNT_MIN)
        )
    }

    val isAtMax = value >= RECORD_AMOUNT_MAX
    val isAtMin = value <= RECORD_AMOUNT_MIN

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        RecordAmountSheetContent(
            value = value,
            isAtMin = isAtMin,
            isAtMax = isAtMax,
            onDecrement = { value = (value - RECORD_AMOUNT_STEP).coerceAtLeast(RECORD_AMOUNT_MIN) },
            onIncrement = { value = (value + RECORD_AMOUNT_STEP).coerceAtMost(RECORD_AMOUNT_MAX) },
            onValueSet = { value = it },
            onSave = {
                onAmountChanged(value.toFloat())
                dismissWithAnimation()
            },
            unitPreference = unitPreference,
        )
    }
}

@Composable
private fun RecordAmountSheetContent(
    value: Int,
    isAtMin: Boolean,
    isAtMax: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onValueSet: (Int) -> Unit,
    onSave: () -> Unit,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val displayUnit = effectiveDisplayUnit(unitPreference, value.toFloat())

    val (minDisplay, maxDisplay) = when (displayUnit) {
        DisplayUnit.ML -> Pair(RECORD_AMOUNT_MIN.toFloat(), RECORD_AMOUNT_MAX.toFloat())
        DisplayUnit.FL_OZ -> Pair(RECORD_AMOUNT_MIN / 29.574f, RECORD_AMOUNT_MAX / 29.574f)
        DisplayUnit.L -> Pair(RECORD_AMOUNT_MIN / 1000f, RECORD_AMOUNT_MAX / 1000f)
    }

    fun formatBound(v: Float) =
        if (displayUnit == DisplayUnit.ML) v.toInt().toString() else "%.1f".format(v)

    fun trySave() {
        val parsed = textInput.trim().toFloatOrNull()
        when {
            parsed == null ->
                errorMessage = context.getString(R.string.validation_error_invalid_number)

            parsed < minDisplay ->
                errorMessage =
                    context.getString(R.string.validation_error_min_value, formatBound(minDisplay))

            parsed > maxDisplay ->
                errorMessage =
                    context.getString(R.string.validation_error_max_value, formatBound(maxDisplay))

            else -> {
                val normalisedDisplay = when (displayUnit) {
                    DisplayUnit.ML -> parsed.roundToInt().toFloat()
                    DisplayUnit.FL_OZ -> (parsed * 10).roundToInt() / 10f
                    DisplayUnit.L -> (parsed * 10).roundToInt() / 10f
                }
                val amountMl = when (displayUnit) {
                    DisplayUnit.ML -> normalisedDisplay
                    DisplayUnit.FL_OZ -> normalisedDisplay * 29.574f
                    DisplayUnit.L -> normalisedDisplay * 1000f
                }
                onValueSet(amountMl.roundToInt())
                isEditing = false
                textInput = ""
                errorMessage = ""
                focusManager.clearFocus()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.history_edit_amount_title),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(32.dp))

        AnimatedContent(
            targetState = isEditing,
            label = "record_amount_stepper_edit",
        ) { editing ->
            if (!editing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RecordAmountStepperButton(
                        enabled = !isAtMin,
                        onClick = onDecrement,
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
                            text = value.toFloat().formatVolume(unitPreference),
                            fontFamily = Nunito,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.clickable { isEditing = true },
                        )
                    }

                    Spacer(Modifier.width(28.dp))

                    RecordAmountStepperButton(
                        enabled = !isAtMax,
                        onClick = onIncrement,
                    ) {
                        Icon(
                            imageVector = DwIcons.Plus,
                            contentDescription = "Increase",
                            tint = if (!isAtMax) DwWater else DwWater.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
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

        Spacer(Modifier.height(16.dp))

        DwPrimaryButton(
            text = stringResource(R.string.action_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RecordAmountStepperButton(
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
