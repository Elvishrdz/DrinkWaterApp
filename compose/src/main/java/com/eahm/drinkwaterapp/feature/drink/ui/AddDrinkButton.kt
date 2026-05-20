package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.drink.drinkSuggestionsMocks
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import com.eahm.drinkwaterapp.ui.inputfield.AppInputField
import kotlinx.coroutines.launch

/** Maximum number of suggestions the user can save. */
private const val MAX_SUGGESTIONS = 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddDrinkButton(
    presets: List<DrinkSuggestion>,
    onDrinkSuggestion: (Float) -> Unit,
    onDrinkCustom: (Float) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onReorder: (List<DrinkSuggestion>, Boolean) -> Unit,
    onDelete: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    onBottomSheetOpened: (Int) -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var canDismissBottomSheet by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { canDismissBottomSheet }
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val suggestionRemovedMsg = stringResource(R.string.drink_suggestion_removed)
    val undoLabel = stringResource(R.string.action_undo)

    var isDragging by remember { mutableStateOf(false) }
    var isOverDeleteZone by remember { mutableStateOf(false) }

    // If the user somehow has MORE than MAX_SUGGESTIONS (legacy data) we display
    // that count as the cap so the counter never shows "17/15". Once they delete
    // down to below MAX_SUGGESTIONS they can create presets again.
    val effectiveMax = maxOf(presets.size, MAX_SUGGESTIONS)
    val canCreatePreset = presets.size < MAX_SUGGESTIONS

    val dismissBottomSheet: () -> Unit = {
        showBottomSheet = false
    }

    ChipButton(
        icon = DwIcons.Plus,
        text = stringResource(R.string.drink_add_more_label),
        onClick = {
            showBottomSheet = true
            onBottomSheetOpened(presets.size)
        },
        modifier = modifier,
    )

    if (showBottomSheet) {
        AppBottomSheet(
            modifier = Modifier.imePadding(),
            onDismissRequest = dismissBottomSheet,
            sheetState = sheetState,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column {
                        Header(
                            onInfoClicked = { showInfoDialog = true },
                        )

                        Spacer(Modifier.height(12.dp))

                        PresetCounter(
                            current = presets.size,
                            max = effectiveMax,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(Alignment.CenterVertically),
                        )

                        Spacer(Modifier.height(12.dp))
                    }

                    if (showInfoDialog) {
                        SuggestionsInfoDialog(
                            onDismiss = { showInfoDialog = false },
                        )
                    }

                    DraggablePresetGrid(
                        modifier = Modifier
                            .wrapContentHeight()
                            .zIndex(1f),
                        presets = presets,
                        onDrink = { amount ->
                            onDrinkSuggestion(amount)
                            dismissBottomSheet()
                        },
                        onReorder = { onReorder(it, false) },
                        unitPreference = unitPreference,
                        onDelete = { preset ->
                            onDelete(preset)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = suggestionRemovedMsg,
                                    actionLabel = undoLabel,
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onRestorePreset(preset)
                                }
                            }
                        },
                        onDraggingChanged = { isDragging = it },
                        onDeleteHover = { isOverDeleteZone = it },
                    )

                    Column {
                        Spacer(Modifier.height(12.dp))

                        Box {
                            InputFieldArea(
                                modifier = Modifier
                                    .imePadding(),
                                onCreatePreset = {
                                    // disable inputs when dragging is active
                                    if (!isDragging) {
                                        onCreatePreset(it)
                                    }
                                },
                                onDrink = { amount ->
                                    // disable inputs when dragging is active
                                    if (!isDragging) {
                                        onDrinkCustom(amount)
                                        dismissBottomSheet()
                                    }
                                },
                                canCreatePreset = canCreatePreset,
                                unitPreference = unitPreference,
                            )
                            if (isDragging) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .matchParentSize(),
                                )
                                DeleteButton(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .zIndex(2f),
                                    isHovered = isOverDeleteZone,
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
fun Header(
    onInfoClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = stringResource(R.string.drink_suggestions_title),
            style = MaterialTheme.typography.titleLarge,
        )

        IconButton(
            modifier = Modifier
                .size(48.dp),
            onClick = onInfoClicked,
        ) {
            Icon(
                imageVector = DwIcons.Info,
                contentDescription = stringResource(R.string.drink_suggestions_info_desc),
                tint = DwInk300,
            )
        }
    }
}

@Composable
private fun SuggestionsInfoDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.drink_suggestions_info_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBullet(stringResource(R.string.drink_suggestions_info_quick_presets))
                InfoBullet(stringResource(R.string.drink_suggestions_info_reorder))
                InfoBullet(stringResource(R.string.drink_suggestions_info_delete))
                InfoBullet(stringResource(R.string.drink_suggestions_info_add))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.drink_suggestions_info_ok))
            }
        },
    )
}

@Composable
private fun InfoBullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class SubUnit { ML, L, FL_OZ }

@Composable
fun InputFieldArea(
    onCreatePreset: (Float) -> Unit,
    onDrink: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    canCreatePreset: Boolean = true,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    // For AUTO_LITERS the user can switch between mL and L; other units are fixed.
    var selectedSubUnit by remember(unitPreference) {
        mutableStateOf(
            when (unitPreference) {
                UnitPreference.FL_OZ -> SubUnit.FL_OZ
                else -> SubUnit.ML
            }
        )
    }

    val effectiveUnit = when (unitPreference) {
        UnitPreference.ML -> SubUnit.ML
        UnitPreference.FL_OZ -> SubUnit.FL_OZ
        UnitPreference.AUTO_LITERS -> selectedSubUnit
    }

    val label = when (effectiveUnit) {
        SubUnit.ML -> stringResource(R.string.drink_custom_amount_label_ml)
        SubUnit.L -> stringResource(R.string.drink_custom_amount_label_l)
        SubUnit.FL_OZ -> stringResource(R.string.drink_custom_amount_label_fl_oz)
    }
    val minVal = when (effectiveUnit) {
        SubUnit.ML -> 1f
        SubUnit.L -> 0.001f
        SubUnit.FL_OZ -> 0.1f
    }
    val maxVal = when (effectiveUnit) {
        SubUnit.ML -> 99_999f
        SubUnit.L -> 99.999f
        SubUnit.FL_OZ -> 99_999f / 29.574f
    }

    // Raw text typed by the user in the display unit
    var inputText by rememberSaveable(effectiveUnit) { mutableStateOf("") }
    var disableButtons by rememberSaveable { mutableStateOf(false) }

    // Convert validated display-unit value to mL for callers
    fun toMl(displayValue: Float): Float = when (effectiveUnit) {
        SubUnit.ML -> displayValue
        SubUnit.L -> displayValue * 1000f
        SubUnit.FL_OZ -> displayValue * 29.574f
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (unitPreference == UnitPreference.AUTO_LITERS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppInputField(
                    modifier = Modifier.weight(1f),
                    label = label,
                    value = inputText,
                    onValueChanged = { inputText = it },
                    onHasError = { disableButtons = it },
                    enabled = enabled,
                )
                SingleChoiceSubUnitToggle(
                    selected = selectedSubUnit,
                    onSelect = { selectedSubUnit = it },
                )
            }
        } else {
            AppInputField(
                modifier = Modifier.fillMaxWidth(),
                label = label,
                value = inputText,
                onValueChanged = { inputText = it },
                onHasError = { disableButtons = it },
                enabled = enabled,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            DwSecondaryButton(
                text = stringResource(R.string.drink_action_create),
                enabled = !disableButtons && enabled && canCreatePreset && inputText.isNotBlank(),
                onClick = {
                    val parsed = inputText.trim().toFloatOrNull() ?: return@DwSecondaryButton
                    if (parsed in minVal..maxVal) onCreatePreset(toMl(parsed))
                },
            )

            Spacer(Modifier.width(7.dp))

            DwPrimaryButton(
                text = stringResource(R.string.drink_action_drink),
                enabled = !disableButtons && enabled && inputText.isNotBlank(),
                onClick = {
                    val parsed = inputText.trim().toFloatOrNull() ?: return@DwPrimaryButton
                    if (parsed in minVal..maxVal) onDrink(toMl(parsed))
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleChoiceSubUnitToggle(
    selected: SubUnit,
    onSelect: (SubUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(SubUnit.ML, SubUnit.L)
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, unit ->
            SegmentedButton(
                selected = unit == selected,
                onClick = { onSelect(unit) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    text = when (unit) {
                        SubUnit.ML -> "mL"
                        SubUnit.L -> "L"
                        SubUnit.FL_OZ -> "fl oz"
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun PresetCounter(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$current / $max",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = modifier,
    )
}

@Composable
internal fun ChipButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DwTintMagenta)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DwMagenta,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DwMagenta
        )
    }
}

@Preview
@Composable
private fun AddDrinkButtonPreview() {
    AddDrinkButton(
        presets = drinkSuggestionsMocks,
        onDrinkSuggestion = {},
        onDrinkCustom = {},
        onCreatePreset = {},
        onReorder = { _, _ -> },
        onDelete = {},
        onRestorePreset = {},
        onBottomSheetOpened = {},
    )
}
