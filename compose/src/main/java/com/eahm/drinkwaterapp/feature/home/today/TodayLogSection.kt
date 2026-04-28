package com.eahm.drinkwaterapp.feature.home.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

data class DrinkLogEntry(val time: String, val amount: Int)

@Composable
fun TodayLogSection(
    items: List<DrinkLogEntry>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DwPaper)
            .border(1.dp, DwInk100, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DwInk900
            )
            TextButton(
                onClick = onSeeAll,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 4.dp,
                    vertical = 0.dp
                )
            ) {
                Text(
                    text = "SEE ALL",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.05.sp,
                    color = DwMagenta
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (items.isEmpty()) {
            Text(
                text = "No drinks logged yet — tap Add water to start.",
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEachIndexed { index, entry ->
                    DrinkLogRow(entry = entry)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            color = DwInk100,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrinkLogRow(entry: DrinkLogEntry) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DwWater)
        ) {
            Icon(
                imageVector = DwIcons.Glass,
                contentDescription = null,
                tint = DwPaper,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "${entry.amount} mL",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DwInk900,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        Text(
            text = entry.time,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = DwInk400
        )
    }
}

val previewLogEntries = listOf(
    DrinkLogEntry("09:14", 250),
    DrinkLogEntry("10:30", 500),
    DrinkLogEntry("11:45", 100),
    DrinkLogEntry("13:02", 250),
    DrinkLogEntry("14:20", 400),
)

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun TodayLogSectionPreview() {
    DrinkWaterAppTheme {
        TodayLogSection(
            items = previewLogEntries,
            onSeeAll = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun TodayLogSectionEmptyPreview() {
    DrinkWaterAppTheme {
        TodayLogSection(
            items = emptyList(),
            onSeeAll = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
