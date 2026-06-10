package com.eahm.drinkwaterapp.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

/**
 * House "remove ads" promo shown in the banner slot when no fillable network ad is returned. Tapping
 * it routes the user to where they can purchase ad removal. Only ever shown to non-purchased users
 * (the banner itself is hidden once ads are removed).
 */
@Composable
internal fun RemoveAdsHouseAd(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = DwTintMagenta,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = DwIcons.Star,
                contentDescription = null,
                tint = DwMagenta,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enjoying the app? Go ad-free",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Remove ads with a one-time purchase",
                    color = DwInk400,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemoveAdsHouseAdPreview() {
    DrinkWaterAppTheme {
        RemoveAdsHouseAd(onClick = {})
    }
}
