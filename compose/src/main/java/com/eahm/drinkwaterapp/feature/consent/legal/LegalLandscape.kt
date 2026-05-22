package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.feature.consent.legal.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalBodyContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent

/**
 * Landscape / foldable-expanded: gradient branding panel on the left,
 * white content surface on the right.
 */
@Composable
internal fun LegalLandscape(
    innerPadding: PaddingValues,
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Left: gradient branding panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(waterGradient),
        ) {
            Bubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LegalHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(22.dp)
                .background(color = MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LegalBodyContent(
                privacyToggleChecked = privacyToggleChecked,
                onToggleChanged = onToggleChanged,
                onAccept = onAccept,
            )
        }
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun LegalLandscapePreview() {
    DrinkWaterAppTheme {
        LegalLandscape(
            innerPadding = PaddingValues(0.dp),
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
