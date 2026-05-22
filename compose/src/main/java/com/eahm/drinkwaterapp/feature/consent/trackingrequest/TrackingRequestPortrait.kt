package com.eahm.drinkwaterapp.feature.consent.trackingrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackButtons
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackHeaderContent
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackMainContent
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

private val TrackWaveHeight = 220.dp
private val TrackOverlapHeight = 25.dp

/**
 * Portrait: wave header at the top, overlapping white sheet at the bottom.
 */
@Composable
internal fun TrackingRequestPortrait(
    innerPadding: PaddingValues,
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        val sheetHeight = maxHeight - (TrackWaveHeight - TrackOverlapHeight)

        // Wave header
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(TrackWaveHeight)
                .zIndex(0f)
                .background(magentaGradient),
        ) {
            Bubbles()
            Column(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 40.dp),
            ) {
                TrackHeaderContent(showStepCounter = showStepCounter, centered = false)
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
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TrackMainContent()
                Spacer(Modifier.weight(1f))
                TrackButtons(
                    isTrackingEnabled = isTrackingEnabled,
                    onAllow = onAllow,
                    onDecline = onDecline,
                )
            }
        }
    }
}

@Preview(name = "Portrait")
@Composable
private fun TrackingRequestPortraitPreview() {
    DrinkWaterAppTheme {
        TrackingRequestPortrait(
            innerPadding = PaddingValues(0.dp),
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}
