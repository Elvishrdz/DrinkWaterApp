package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk700
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.settings.WaterUnit
import com.eahm.drinkwaterapp.feature.settings.WeightUnit
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet

/** Maps a [WaterUnit] to its paired [WeightUnit] (metric ↔ imperial). */
private fun WaterUnit.pairedWeightUnit() = when (this) {
    WaterUnit.ML -> WeightUnit.KG
    WaterUnit.FL_OZ -> WeightUnit.LBS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UnitsBottomSheet(
    currentUnit: WaterUnit,
    onUnitChanged: (WaterUnit, WeightUnit) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.settings_row_units),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UnitOption(
                    label = stringResource(R.string.settings_unit_metric_label),
                    sublabel = stringResource(R.string.settings_unit_metric_sublabel),
                    selected = currentUnit == WaterUnit.ML,
                    onClick = {
                        onUnitChanged(WaterUnit.ML, WaterUnit.ML.pairedWeightUnit())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                )
                UnitOption(
                    label = stringResource(R.string.settings_unit_imperial_label),
                    sublabel = stringResource(R.string.settings_unit_imperial_sublabel),
                    selected = currentUnit == WaterUnit.FL_OZ,
                    onClick = {
                        onUnitChanged(WaterUnit.FL_OZ, WaterUnit.FL_OZ.pairedWeightUnit())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UnitOption(
    label: String,
    sublabel: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val bgColor = if (selected) DwTintMagenta else Color.Transparent
    val borderColor = if (selected) DwMagenta else DwInk100
    val labelColor = if (selected) DwMagenta else DwInk700
    val sublabelColor = if (selected) DwMagenta.copy(alpha = 0.7f) else DwInk700.copy(alpha = 0.6f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.5.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = labelColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = sublabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = sublabelColor,
            )
        }
    }
}
