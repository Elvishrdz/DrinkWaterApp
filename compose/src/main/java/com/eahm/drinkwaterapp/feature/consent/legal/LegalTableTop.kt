package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.eahm.drinkwaterapp.feature.consent.legal.ui.AcceptLegalCheckbox
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalMainButton
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalScrollableContent
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

/**
 * Table-top: gradient branding panel on the top half,
 * white content surface on the bottom half.
 */
@Composable
internal fun LegalTableTop(
    innerPadding: PaddingValues,
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Top: gradient branding panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

        // Bottom: white surface — left scrollable content, right controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset(0.dp, (-25).dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .padding(top = 25.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Left: scrollable policy content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    LegalScrollableContent()
                }

                // Right: agree row + CTA, centered vertically
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                ) {
                    AcceptLegalCheckbox(
                        privacyToggleChecked = privacyToggleChecked,
                        onToggleChanged = onToggleChanged,
                    )

                    LegalMainButton(
                        onClick = onAccept,
                        enabled = privacyToggleChecked,
                    )
                }
            }
        }
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun LegalTableTopPreview() {
    DrinkWaterAppTheme {
        LegalTableTop(
            innerPadding = PaddingValues(0.dp),
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
