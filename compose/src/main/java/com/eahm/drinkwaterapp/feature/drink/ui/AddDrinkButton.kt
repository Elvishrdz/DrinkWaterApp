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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import com.eahm.drinkwaterapp.feature.drink.drinkSuggestionsMocks
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import com.eahm.drinkwaterapp.ui.inputfield.AppInputField
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddDrinkButton(
    presets: List<DrinkSuggestion>,
    onDrink: (Float) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onReorder: (List<DrinkSuggestion>, Boolean) -> Unit,
    onDelete: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
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

    val dismissBottomSheet: () -> Unit = {
        showBottomSheet = false
    }

    ChipButton(
        icon = DwIcons.Plus,
        text = stringResource(R.string.drink_add_more_label),
        onClick = { showBottomSheet = true },
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
                            onCloseClicked = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        dismissBottomSheet()
                                    }
                                }
                            },
                        )

                        Spacer(Modifier.height(24.dp))
                    }

                    DraggablePresetGrid(
                        modifier = Modifier
                            .wrapContentHeight()
                            //.verticalScroll(rememberScrollState()) ,
                            .zIndex(1f),
                        presets = presets,
                        onDrink = { amount ->
                            onDrink(amount)
                            dismissBottomSheet()
                        },
                        onReorder = { onReorder(it, false) },
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
                                onCreatePreset = {
                                    // disable inputs when dragging is active
                                    if (!isDragging) {
                                        onCreatePreset(it)
                                    }
                                },
                                onDrink = { amount ->
                                    // disable inputs when dragging is active
                                    if (!isDragging) {
                                        onDrink(amount)
                                        dismissBottomSheet()
                                    }
                                },
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
    onCloseClicked: () -> Unit,
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
            onClick = onCloseClicked,
        ) {
            Icon(
                imageVector = DwIcons.Close,
                contentDescription = stringResource(R.string.drink_suggestions_close_desc),
                tint = DwInk300,
            )
        }
    }
}

@Composable
fun InputFieldArea(
    onCreatePreset: (Float) -> Unit,
    onDrink: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var customAmount by rememberSaveable { mutableStateOf<Float?>(null) }
    var disableButtons by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        AppInputField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.drink_custom_amount_label),
            value = customAmount?.toDigitString().orEmpty(),
            onValueChanged = {
                customAmount = it.toFloatOrNull()
            },
            onHasError = {
                disableButtons = it
            },
            enabled = enabled,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            DwSecondaryButton(
                text = stringResource(R.string.drink_action_create),
                enabled = !disableButtons && enabled,
                onClick = {
                    customAmount?.let {
                        onCreatePreset(it)
                    }
                },
            )

            Spacer(Modifier.width(7.dp))

            DwPrimaryButton(
                text = stringResource(R.string.drink_action_drink),
                enabled = !disableButtons && enabled,
                onClick = {
                    customAmount?.let {
                        onDrink(it)
                    }
                },
            )
        }
    }
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
        onDrink = {},
        onCreatePreset = {},
        onReorder = { _, _ -> },
        onDelete = {},
        onRestorePreset = {},
    )
}
