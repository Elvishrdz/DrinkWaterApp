package com.eahm.drinkwaterapp.feature.dev.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.pad
import kotlin.math.roundToInt

@Composable
internal fun ScheduleSlotChips(schedule: DrinkSchedule) {
    Text(
        text = "Schedule",
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = DwInk500,
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        schedule.slots.forEachIndexed { idx, slot ->
            SlotChip(
                time = "${slot.time.hour.pad()}:${slot.time.minute.pad()}",
                amount = "${slot.amountMl.roundToInt()}mL",
                isEdge = idx == 0 || idx == schedule.slots.lastIndex,
            )
        }
    }
}

@Composable
private fun SlotChip(
    time: String,
    amount: String,
    isEdge: Boolean,
) {
    val bg by animateColorAsState(
        targetValue = if (isEdge) DwTintMagenta else DwTintWater,
        label = "chip_bg",
    )
    Column(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = time,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = if (isEdge) DwMagenta else DwWaterDeep,
        )
        Text(
            text = amount,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = DwInk500,
        )
    }
}