package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
internal fun LegalLandscape(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .background(legalHeaderGradient),
        ) {
            Bubbles(
                modifier = Modifier
                    .matchParentSize()
                    .background(legalHeaderGradient),
            )

            LegalHeaderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
                    )
                    .padding(all = 22.dp),
                showStepCounter = showStepCounter,
                centered = true,
            )
        }

        val horizontalPadding = 22.dp
        val spaceBetweenDp = 14.dp

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.End),
                    )
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = horizontalPadding,
                    ),
            ) {
                LegalScrollableContent(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    contentPadding = windowInsets
                        .only(WindowInsetsSides.Top)
                        .asPaddingValues()
                )

                Spacer(modifier = Modifier.height(spaceBetweenDp))

                Column(
                    modifier = Modifier
                        .windowInsetsPadding(
                            insets = windowInsets.only(WindowInsetsSides.Bottom),
                        ),
                    verticalArrangement = Arrangement.spacedBy(spaceBetweenDp)
                ) {
                    LegalWebLinkButton(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .align(Alignment.CenterHorizontally),
                    )

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

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun LegalLandscapePreview() {
    DrinkWaterAppTheme {
        LegalLandscape(
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}

@Preview(widthDp = 900, heightDp = 420, fontScale = 1.5f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        LegalLandscape(
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
