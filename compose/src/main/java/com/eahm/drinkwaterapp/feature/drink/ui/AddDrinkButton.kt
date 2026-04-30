package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.feature.drink.drinkPresetsMock
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import com.eahm.drinkwaterapp.ui.inputfield.AppInputField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddDrinkButton(
    presets: List<DrinkPreset>,
    onDrink: (Float) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onReorder: (List<DrinkPreset>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var canDismissBottomSheet by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { canDismissBottomSheet }
    )
    val scope = rememberCoroutineScope()

    val dismissBottomSheet: () -> Unit = {
        showBottomSheet = false
    }

    ChipButton(
        icon = DwIcons.Plus,
        text = "More",
        onClick = { showBottomSheet = true },
        modifier = modifier,
    )

    if (showBottomSheet) {
        AppBottomSheet(
            modifier = Modifier.imePadding(),
            onDismissRequest = dismissBottomSheet,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
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
                        text = "Drink Presets",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    IconButton(
                        modifier = Modifier
                            .size(48.dp),
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    dismissBottomSheet()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = DwIcons.Trash,
                            contentDescription = "Close",
                            tint = DwInk300,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                DraggablePresetGrid(
                    presets = presets,
                    onDrink = { amount ->
                        onDrink(amount)
                        dismissBottomSheet()
                    },
                    onReorder = onReorder,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    AppInputField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Custom Amount",
                        value = "",
                        onValueChanged = {},
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        DwSecondaryButton(
                            text = "Create",
                            onClick = {
                                // TODO: disable  dismiss when dragging item
                                // canDismissBottomSheet = false
                            },
                        )

                        Spacer(Modifier.width(7.dp))

                        DwPrimaryButton(
                            text = "Drink",
                            onClick = {},
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
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
        presets = drinkPresetsMock,
        onDrink = {},
        onCreatePreset = {},
        onReorder = {},
    )
}
