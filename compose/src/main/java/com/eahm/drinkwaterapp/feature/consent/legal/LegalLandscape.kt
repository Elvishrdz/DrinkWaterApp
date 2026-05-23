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
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.legal.ui.AcceptLegalCheckbox
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalMainButton
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalScrollableContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalWebLinkButton
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.contentPaddingDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.legalGradient
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.verticalSpacingDp

@Composable
internal fun LegalLandscape(
    weight: Float,
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
                .weight(weight)
                .fillMaxHeight()
                .background(legalGradient),
        ) {
            Bubbles(
                modifier = Modifier
                    .matchParentSize()
                    .background(legalGradient),
            )

            LegalHeaderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
                    )
                    .padding(all = contentPaddingDp),
                showStepCounter = showStepCounter,
                centered = true,
            )
        }

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
                        start = contentPaddingDp,
                        end = contentPaddingDp,
                        bottom = contentPaddingDp,
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

                Spacer(modifier = Modifier.height(verticalSpacingDp))

                Column(
                    modifier = Modifier
                        .windowInsetsPadding(
                            insets = windowInsets.only(WindowInsetsSides.Bottom),
                        ),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacingDp)
                ) {
                    LegalWebLinkButton(
                        modifier = Modifier
                            .padding(horizontal = contentPaddingDp)
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
            weight = 1f,
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
            weight = 1f,
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
