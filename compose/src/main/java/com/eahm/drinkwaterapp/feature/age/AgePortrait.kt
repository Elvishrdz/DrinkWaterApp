package com.eahm.drinkwaterapp.feature.age

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.age.ui.AgeHeaderContent
import com.eahm.drinkwaterapp.feature.age.ui.AgeOptionCard
import com.eahm.drinkwaterapp.feature.age.ui.AgePrivacyNote
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.contentPaddingDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.legalGradient
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.overlapHeightDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.shadowElevationDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.topRoundedShape
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.verticalSpacingDp

@Composable
internal fun AgePortrait(
    selected: AgeBracketOption?,
    onSelect: (AgeBracketOption) -> Unit,
    onContinue: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Bubbles(
                modifier = Modifier
                    .matchParentSize()
                    .background(legalGradient),
            )

            AgeHeaderContent(
                centered = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .padding(all = contentPaddingDp)
                    .padding(bottom = overlapHeightDp),
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 480.dp)
                        .padding(horizontal = contentPaddingDp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacingDp),
                ) {
                    Spacer(modifier = Modifier.height(overlapHeightDp))

                    AgeBracketOption.entries.forEach { option ->
                        AgeOptionCard(
                            text = stringResource(option.labelRes),
                            selected = selected == option,
                            onClick = { onSelect(option) },
                        )
                    }

                    AgePrivacyNote()
                }

                Spacer(modifier = Modifier.height(verticalSpacingDp))

                DwPrimaryButton(
                    text = stringResource(R.string.age_collection_continue),
                    enabled = selected != null,
                    onClick = onContinue,
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
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
private fun AgePortraitPreview() {
    DrinkWaterAppTheme {
        AgePortrait(
            selected = AgeBracketOption.ADULT_18_PLUS,
            onSelect = {},
            onContinue = {},
        )
    }
}

@Preview(fontScale = 1.5f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        AgePortrait(
            selected = null,
            onSelect = {},
            onContinue = {},
        )
    }
}
