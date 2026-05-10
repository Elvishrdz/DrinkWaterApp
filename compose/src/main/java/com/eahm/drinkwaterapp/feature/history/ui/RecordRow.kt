package com.eahm.drinkwaterapp.feature.history.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwDanger
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI
import kotlinx.datetime.LocalDateTime
import java.util.Locale

@Composable
internal fun RecordRow(
    item: HistoryUI,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DwTintWater),
            ) {
                Icon(
                    imageVector = DwIcons.Glass,
                    contentDescription = null,
                    tint = DwWater,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.amount} mL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DwInk900,
                )
                Text(
                    text = formatTime12h(item.timestamp),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = DwInk400,
                )
            }

            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = null,
                tint = DwInk300,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 90f else 0f),
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DwWater),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DwWater),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.Ruler,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DwDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DwDanger),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.Trash,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

private fun formatTime12h(dt: LocalDateTime): String {
    val h = dt.hour
    val m = dt.minute
    val period = if (h < 12) "a.m." else "p.m."
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, period)
}



private val previewItem = HistoryUI(
    id = "1",
    amount = "250",
    status = 1,
    timestamp = LocalDateTime(2026, 5, 10, 8, 30),
)

@Preview(showBackground = true)
@Composable
private fun RecordRowCollapsedPreview() {
    DrinkWaterAppTheme {
        RecordRow(
            item = previewItem,
            isExpanded = false,
            onToggle = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordRowExpandedPreview() {
    DrinkWaterAppTheme {
        RecordRow(
            item = previewItem,
            isExpanded = true,
            onToggle = {},
            onEdit = {},
            onDelete = {},
        )
    }
}
