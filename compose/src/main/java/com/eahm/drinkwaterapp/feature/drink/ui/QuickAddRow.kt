package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.feature.drink.drinkPresetsMock
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import kotlinx.coroutines.launch

@Composable
fun QuickAddRow(
    quickPresets: List<DrinkPreset>,
    allPresets: List<DrinkPreset>,
    onDrink: (Float) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onReorder: (List<DrinkPreset>, Boolean) -> Unit,
    onDelete: (DrinkPreset) -> Unit,
    onRestorePreset: (DrinkPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    DraggableQuickAddRow(
        presets = quickPresets,
        onDrink = onDrink,
        onReorder = onReorder,
        modifier = modifier,
        trailingContent = { trailingModifier ->
            AddDrinkButton(
                presets = allPresets,
                onDrink = onDrink,
                onCreatePreset = onCreatePreset,
                onReorder = onReorder,
                onDelete = onDelete,
                onRestorePreset = onRestorePreset,
                modifier = trailingModifier,
            )
        },
    )
}

@Composable
fun WaterChip(
    amount: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .outlineBorderStyle()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = DwIcons.Glass,
            contentDescription = null,
            tint = DwWater,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DwInk900
                    )
                ) {
                    append(amount.toDigitString())
                }
                withStyle(
                    SpanStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = DwInk400
                    )
                ) {
                    append(" mL")
                }
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun QuickAddRowPreview() {
    DrinkWaterAppTheme {
        QuickAddRow(
            quickPresets = drinkPresetsMock.take(3),
            allPresets = drinkPresetsMock,
            onDrink = {},
            onCreatePreset = {},
            onReorder = { _, _ -> },
            onDelete = {},
            onRestorePreset = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
