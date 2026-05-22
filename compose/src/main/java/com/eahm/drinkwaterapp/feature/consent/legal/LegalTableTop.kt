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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.legal.ui.AcceptLegalCheckbox
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalHeaderContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalMainButton
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalScrollableContent
import com.eahm.drinkwaterapp.feature.consent.legal.ui.LegalWebLinkButton
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles

@Composable
internal fun LegalTableTop(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    val allSidesPadding = 22.dp
    val overlapDp = 22.dp

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
                    .background(legalHeaderGradient),
            )

            LegalHeaderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .padding(horizontal = allSidesPadding)
                    .verticalScroll(rememberScrollState()),
                contentPadding = PaddingValues(
                    top = windowInsets.only(WindowInsetsSides.Top).asPaddingValues()
                        .calculateTopPadding() + allSidesPadding,
                    bottom = allSidesPadding + overlapDp,
                ),
                showStepCounter = showStepCounter,
                centered = true,
            )
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val overlapPx = overlapDp.roundToPx()
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
            shape = RoundedCornerShape(topStart = overlapDp, topEnd = overlapDp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    LegalScrollableContent(
                        contentPadding = PaddingValues(
                            start = allSidesPadding + windowInsets
                                .only(WindowInsetsSides.Start)
                                .asPaddingValues()
                                .calculateStartPadding(LayoutDirection.Ltr),
                            end = allSidesPadding,
                            top = allSidesPadding + overlapDp,
                            bottom = allSidesPadding + windowInsets
                                .only(WindowInsetsSides.Bottom)
                                .asPaddingValues()
                                .calculateBottomPadding(),
                        )
                    )
                }

                val acceptAreaScrollState = rememberScrollState()

                // set end position as initial state
                LaunchedEffect(Unit) { acceptAreaScrollState.scrollTo(acceptAreaScrollState.maxValue) }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(acceptAreaScrollState)
                        .padding(
                            start = allSidesPadding,
                            end = allSidesPadding + windowInsets
                                .only(WindowInsetsSides.End)
                                .asPaddingValues()
                                .calculateEndPadding(LayoutDirection.Ltr),
                        ),
                    verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom),
                ) {
                    Spacer(Modifier.height(allSidesPadding))

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
                            allSidesPadding + windowInsets
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
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
