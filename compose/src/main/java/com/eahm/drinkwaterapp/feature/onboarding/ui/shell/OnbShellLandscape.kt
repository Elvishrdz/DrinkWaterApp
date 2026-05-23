package com.eahm.drinkwaterapp.feature.onboarding.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults.contentPaddingDp
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults.externalPaddingDp
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults.topRoundedShape
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbTopAppBar

@Composable
internal fun OnbShellLandscape(
    step: Int,
    total: Int,
    title: String,
    subtitle: String?,
    art: (@Composable () -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Row(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight(),
        ) {
            GradientBackground()
            OnbBubbles()
            Column(
                modifier = Modifier
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                    )
                    .fillMaxSize(),
            ) {
                OnbTopAppBar(
                    step = step,
                    total = total,
                    onBack = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = externalPaddingDp),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .padding(horizontal = externalPaddingDp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (art != null) {
                            Box(
                                modifier = Modifier.padding(top = contentPaddingDp, bottom = 10.dp),
                                contentAlignment = Alignment.Center,
                                content = { art() },
                            )
                        } else {
                            Spacer(Modifier.height(contentPaddingDp))
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )

                        // display same content to center the text properly + add bottom inset
                        // display extra content only if we can't scroll which means there is enough
                        // area to center the text
                        // TODO: fix this only if we can scroll
                        // only if BEFORE we can scroll we add this. its complex
                        if (scrollState.maxValue != Int.MAX_VALUE) {
                            Box(
                                modifier = Modifier
                                    .padding(top = contentPaddingDp, bottom = 10.dp)
                                    .alpha(0f),
                                contentAlignment = Alignment.Center,
                                content = { art?.invoke() },
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(
                                height = windowInsets.only(WindowInsetsSides.Bottom)
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                            )
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = topRoundedShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.End),
                    )
                    .padding(horizontal = externalPaddingDp),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                ) {
                    Spacer(
                        modifier = Modifier.height(
                            height = windowInsets.only(WindowInsetsSides.Top)
                                .asPaddingValues()
                                .calculateTopPadding()
                        )
                    )

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(12.dp))
                    }

                    content()
                }
                Spacer(Modifier.height(12.dp))
                footer()
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun OnbShellLandscapePreview() {
    DrinkWaterAppTheme {
        OnbShellLandscape(
            step = 7,
            total = OnboardingSteps.totalSteps,
            title = "Alarms that arrive on time",
            subtitle = "Allow the app to schedule precise alarms so your reminders fire exactly when you expect them.",
            art = { OnbArtCircle(icon = OnbIcons.Check) },
            onBack = {},
            footer = {
                DwPrimaryButton(
                    text = "Allow exact alarms",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) { /* content placeholder */ }
    }
}
