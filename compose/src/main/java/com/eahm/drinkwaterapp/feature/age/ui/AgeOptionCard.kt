package com.eahm.drinkwaterapp.feature.age.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun AgeOptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)

    Surface(
        shape = shape,
        color = if (selected) DwTintMagenta else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) DwMagenta else DwInk100,
        ),
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = DwMagenta,
                    unselectedColor = DwInk400,
                ),
                modifier = Modifier.padding(12.dp),
            )
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) DwMagenta else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun AgeOptionCardPreview() {
    DrinkWaterAppTheme {
        AgeOptionCard(
            text = "18 or older",
            selected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun UnselectedPreview() {
    DrinkWaterAppTheme {
        AgeOptionCard(
            text = "18 or older",
            selected = false,
            onClick = {},
        )
    }
}
