package com.eahm.drinkwaterapp.feature.age

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.age.ui.AgeHeaderContent
import com.eahm.drinkwaterapp.feature.age.ui.AgeOptionCard
import com.eahm.drinkwaterapp.feature.age.ui.AgePrivacyNote
import com.eahm.drinkwaterapp.feature.consent.ui.Bubbles
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.contentPaddingDp
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.legalGradient
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.verticalSpacingDp

@Composable
internal fun AgeLandscape(
    weight: Float,
    selected: AgeBracketOption?,
    onSelect: (AgeBracketOption) -> Unit,
    onContinue: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Row(
        modifier = Modifier.fillMaxSize(),
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
                    )
                    .padding(all = contentPaddingDp),
                contentAlignment = Alignment.Center,
            ) {
                AgeHeaderContent(
                    centered = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                )
            }
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            windowInsets
                                .only(WindowInsetsSides.Top)
                                .asPaddingValues()
                        ),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacingDp),
                ) {
                    Spacer(modifier = Modifier.height(contentPaddingDp))

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

                Column(
                    modifier = Modifier
                        .windowInsetsPadding(
                            insets = windowInsets.only(WindowInsetsSides.Bottom),
                        ),
                ) {
                    DwPrimaryButton(
                        text = stringResource(R.string.age_collection_continue),
                        enabled = selected != null,
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun AgeLandscapePreview() {
    DrinkWaterAppTheme {
        AgeLandscape(
            weight = 1f,
            selected = AgeBracketOption.TEEN_13_17,
            onSelect = {},
            onContinue = {},
        )
    }
}

@Preview(widthDp = 900, heightDp = 420, fontScale = 1.5f)
@Composable
private fun BigFontPreview() {
    DrinkWaterAppTheme {
        AgeLandscape(
            weight = 1f,
            selected = null,
            onSelect = {},
            onContinue = {},
        )
    }
}
