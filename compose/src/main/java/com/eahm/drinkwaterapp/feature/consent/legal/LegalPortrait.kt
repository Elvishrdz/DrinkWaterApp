package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.feature.consent.legal.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalBodyContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent

private val LegalWaveHeight = 248.dp
private val LegalOverlapHeight = 25.dp

@Composable
internal fun LegalPortrait(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        val sheetHeight = maxHeight - (LegalWaveHeight - LegalOverlapHeight)

        // Wave header
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(LegalWaveHeight)
                .zIndex(0f)
                .background(waterGradient),
        ) {
            Bubbles()
            Column(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 40.dp),
            ) {
                LegalHeaderContent(showStepCounter = showStepCounter, centered = false)
            }
        }

        // Overlapping white sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .align(Alignment.BottomStart)
                .zIndex(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(22.dp),
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
}

@Preview(name = "Portrait")
@Composable
private fun LegalPortraitPreview() {
    DrinkWaterAppTheme {
        LegalScreen(
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
