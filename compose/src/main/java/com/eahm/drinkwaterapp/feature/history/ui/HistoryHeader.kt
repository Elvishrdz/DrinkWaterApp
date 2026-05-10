package com.eahm.drinkwaterapp.feature.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun HistoryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DwPaper)
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "History",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = DwInk900,
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = DwIcons.History,
                contentDescription = "Pick a date",
                tint = DwMagenta,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun HistoryHeaderPreview() {
    DrinkWaterAppTheme {
        HistoryHeader()
    }
}
