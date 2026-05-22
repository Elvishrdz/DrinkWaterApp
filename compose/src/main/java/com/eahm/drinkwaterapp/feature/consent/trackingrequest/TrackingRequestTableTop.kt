package com.eahm.drinkwaterapp.feature.consent.trackingrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackButtons
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackHeaderContent
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackMainContent
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

/**
 * Table-top: gradient branding panel on the top half,
 * white content surface on the bottom half.
 */
@Composable
internal fun TrackingRequestTableTop(
    innerPadding: PaddingValues,
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

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
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(magentaGradient),
        ) {
            Bubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TrackHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        // Bottom: white surface — scrollable content area + buttons pinned at bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TrackMainContent()
                }
                Spacer(Modifier.height(14.dp))
                TrackButtons(
                    isTrackingEnabled = isTrackingEnabled,
                    onAllow = onAllow,
                    onDecline = onDecline,
                )
            }
        }
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun TrackingRequestTableTopPreview() {
    DrinkWaterAppTheme {
        TrackingRequestTableTop(
            innerPadding = PaddingValues(0.dp),
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}
