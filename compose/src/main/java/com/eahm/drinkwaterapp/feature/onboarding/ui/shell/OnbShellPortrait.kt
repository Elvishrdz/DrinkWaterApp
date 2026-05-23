package com.eahm.drinkwaterapp.feature.onboarding.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbTopAppBar

internal val externalPaddingDp = 7.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnbShellPortrait(
    step: Int,
    total: Int,
    title: String,
    subtitle: String?,
    art: (@Composable () -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    scrollableContent: @Composable ColumnScope.() -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        OnbBubbles()

        Column {
            OnbTopAppBar(
                onBack = onBack,
                step = step,
                total = total,
                modifier = Modifier
                    .windowInsetsPadding(
                        windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    )
                    .fillMaxWidth()
                    .padding(horizontal = externalPaddingDp),
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .windowInsetsPadding(
                        windowInsets.only(WindowInsetsSides.Horizontal)
                    )
                    .fillMaxSize()
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .padding(horizontal = externalPaddingDp),
                shape = RoundedCornerShape(topStart = BodyCornerRadius, topEnd = BodyCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (art != null) {
                            Box(
                                modifier = Modifier.padding(top = 15.dp, bottom = 10.dp),
                                contentAlignment = Alignment.Center,
                                content = { art() },
                            )
                        } else {
                            Spacer(Modifier.height(30.dp))
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        if (subtitle != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f),
                    ) {
                        scrollableContent()
                    }

                    Spacer(Modifier.height(12.dp))
                    footer()
                    Spacer(Modifier.height(12.dp))

                    Spacer(
                        Modifier.height(
                            height = windowInsets.only(WindowInsetsSides.Bottom)
                                .asPaddingValues()
                                .calculateBottomPadding(),
                        )
                    )
                }
            }
        }
    }
}

@DwPreview
@Composable
private fun NoBackButtonPreview() {
    DrinkWaterAppTheme {
        OnbShellPortrait(
            step = 1,
            total = OnboardingSteps.totalSteps,
            title = "Let's build a great habit",
            subtitle = "In the next steps we'll quickly set up your hydration goal and reminders.",
            art = null,
            onBack = null,
            footer = {
                DwPrimaryButton(
                    text = "Get started",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) { /* content placeholder */ }
    }
}

@DwPreview
@Composable
private fun BackButtonAndArtPreview() {
    DrinkWaterAppTheme {
        OnbShellPortrait(
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
