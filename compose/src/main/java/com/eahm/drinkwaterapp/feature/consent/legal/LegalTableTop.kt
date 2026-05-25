package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.legal.ui.AcceptLegalCheckbox
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalMainButton
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalScrollableContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalWebLinkButton
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.contentPaddingDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.legalGradient
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.overlapHeightDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.shadowElevationDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.topRoundedShape
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.verticalSpacingDp

@Composable
internal fun LegalTableTop(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    showNewBadge: Boolean,
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
                .fillMaxWidth()
                .weight(1f),
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
                        insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .padding(horizontal = contentPaddingDp)
                    .verticalScroll(rememberScrollState()),
                contentPadding = PaddingValues(
                    top = windowInsets.only(WindowInsetsSides.Top).asPaddingValues()
                        .calculateTopPadding() + contentPaddingDp,
                    bottom = contentPaddingDp + overlapHeightDp,
                ),
                showStepCounter = showStepCounter,
                showNewBadge = showNewBadge,
                centered = true,
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
            Row(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacingDp),
                ) {
                    LegalScrollableContent(
                        contentPadding = PaddingValues(
                            start = contentPaddingDp + windowInsets
                                .only(WindowInsetsSides.Start)
                                .asPaddingValues()
                                .calculateStartPadding(LayoutDirection.Ltr),
                            end = contentPaddingDp,
                            top = contentPaddingDp + overlapHeightDp,
                            bottom = contentPaddingDp + windowInsets
                                .only(WindowInsetsSides.Bottom)
                                .asPaddingValues()
                                .calculateBottomPadding(),
                        )
                    )
                }

                val ctaScrollState = rememberScrollState()

                // set end position as initial state
                LaunchedEffect(Unit) { ctaScrollState.scrollTo(ctaScrollState.maxValue) }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(ctaScrollState)
                        .padding(
                            start = contentPaddingDp,
                            end = contentPaddingDp + windowInsets
                                .only(WindowInsetsSides.End)
                                .asPaddingValues()
                                .calculateEndPadding(LayoutDirection.Ltr),
                        ),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacingDp, Alignment.Bottom),
                ) {
                    Spacer(Modifier.height(contentPaddingDp))

                    LegalWebLinkButton(
                        modifier = Modifier
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

                    Spacer(
                        Modifier.height(
                            contentPaddingDp + windowInsets
                                .only(WindowInsetsSides.Bottom)
                                .asPaddingValues()
                                .calculateBottomPadding(),
                        )
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
            privacyToggleChecked = false,
            showStepCounter = true,
            showNewBadge = false,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700, fontScale = 3f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        LegalTableTop(
            privacyToggleChecked = false,
            showStepCounter = true,
            showNewBadge = false,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
