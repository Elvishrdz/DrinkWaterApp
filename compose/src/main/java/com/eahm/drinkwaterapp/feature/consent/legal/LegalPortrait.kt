package com.eahm.drinkwaterapp.feature.consent.legal

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.legal.ui.AcceptLegalCheckbox
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalMainButton
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalScrollableContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalWebLinkButton
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

@Composable
internal fun LegalPortrait(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
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
                    .background(legalHeaderGradient),
            )

            LegalHeaderContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .padding(all = contentPaddingDp)
                    .padding(bottom = overlapHeightDp),
                showStepCounter = showStepCounter,
                centered = false
            )
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val overlapPx = overlapHeightDp.roundToPx()

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
            shape = topRoundedShape,
            shadowElevation = shadowElevationDp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(),
            ) {
                LegalScrollableContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = contentPaddingDp)
                        .verticalScroll(rememberScrollState()),
                    contentPadding = PaddingValues(
                        top = overlapHeightDp,
                    )
                )

                Spacer(modifier = Modifier.height(verticalSpacingDp))

                LegalWebLinkButton(
                    modifier = Modifier
                        .padding(horizontal = contentPaddingDp)
                        .align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(verticalSpacingDp))

                AcceptLegalCheckbox(
                    privacyToggleChecked = privacyToggleChecked,
                    onToggleChanged = onToggleChanged,
                    modifier = Modifier
                        .padding(horizontal = contentPaddingDp),
                )

                Spacer(modifier = Modifier.height(verticalSpacingDp))

                LegalMainButton(
                    onClick = onAccept,
                    enabled = privacyToggleChecked,
                    modifier = Modifier
                        .padding(horizontal = contentPaddingDp),
                )

                Spacer(modifier = Modifier.height(overlapHeightDp))

                Spacer(
                    modifier = Modifier
                        .windowInsetsPadding(
                            insets = windowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                        ),
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

@Preview(fontScale = 1.5f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        LegalScreen(
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
