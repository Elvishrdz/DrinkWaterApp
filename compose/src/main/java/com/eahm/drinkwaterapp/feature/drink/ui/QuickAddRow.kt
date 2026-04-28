package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito


private val presets = listOf(100, 250, 500)

@Composable
fun QuickAddRow(
    onDrink: (Float) -> Unit,
    onCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { amount ->
            WaterChip(
                amount = amount,
                onClick = { onDrink(amount.toFloat()) },
                modifier = Modifier.weight(1f)
            )
        }
        CustomChip(
            onClick = onCustom,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WaterChip(
    amount: Int,
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
                    append(amount.toString())
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

@Composable
private fun CustomChip(
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
            imageVector = DwIcons.Plus,
            contentDescription = null,
            tint = DwMagenta,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Custom",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DwMagenta
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun QuickAddRowPreview() {
    DrinkWaterAppTheme {
        QuickAddRow(
            onDrink = {},
            onCustom = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
