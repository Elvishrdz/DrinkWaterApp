package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DwTintWater)
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 7.dp,
                bottom = 10.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            color = DwInk500,
        )

        Text(
            text = value,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = DwWaterDeep,
        )
    }
}

@Preview
@Composable
private fun SummaryChipPreview() {
    DrinkWaterAppTheme {
        SummaryChip("label", "value")
    }
}
