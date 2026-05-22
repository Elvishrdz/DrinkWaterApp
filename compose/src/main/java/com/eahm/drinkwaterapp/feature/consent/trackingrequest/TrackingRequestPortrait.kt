package com.eahm.drinkwaterapp.feature.consent.trackingrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.legal.contentPaddingDp
import com.eahm.drinkwaterapp.feature.consent.legal.overlapHeightDp
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackButtons
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackHeaderContent
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui.TrackMainContent
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

@Composable
internal fun TrackingRequestPortrait(
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Bubbles(
                modifier = Modifier
                    .matchParentSize()
                    .background(trackMagentaGradient),
            )

            TrackHeaderContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .padding(all = trackContentPaddingDp)
                    .padding(bottom = trackOverlapHeightDp),
                showStepCounter = showStepCounter,
                centered = false
            )
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val overlapPx = trackOverlapHeightDp.roundToPx()

                    val placeable = measurable.measure(
                        constraints.copy(
                            minHeight = constraints.minHeight + overlapPx,
                            maxHeight = constraints.maxHeight + overlapPx,
                        )
                    )

                    layout(placeable.width, constraints.maxHeight) {
                        placeable.placeRelative(0, -overlapPx)
                    }
                },
            shape = trackTopRoundedShape,
            shadowElevation = trackShadowElevationDp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
            ) {
                TrackMainContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = contentPaddingDp)
                        .verticalScroll(rememberScrollState()),
                    contentPadding = PaddingValues(
                        top = overlapHeightDp,
                    )
                )

                Spacer(modifier = Modifier.height(trackVerticalSpacingDp))

                Box(modifier = Modifier.padding(horizontal = trackContentPaddingDp)) {
                    TrackButtons(
                        isTrackingEnabled = isTrackingEnabled,
                        onAllow = onAllow,
                        onDecline = onDecline,
                    )
                }

                Spacer(modifier = Modifier.height(trackOverlapHeightDp))

                Spacer(
                    modifier = Modifier.windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                    ),
                )
            }
        }
    }
}

@Preview(name = "Portrait")
@Composable
private fun TrackingRequestPortraitPreview() {
    DrinkWaterAppTheme {
        TrackingRequestScreen(
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}

@Preview(fontScale = 1.5f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        TrackingRequestScreen(
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}
