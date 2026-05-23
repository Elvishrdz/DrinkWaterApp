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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.ui.ConsentDefaults.overlapHeightDp
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults.externalPaddingDp
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbShellDefaults.topRoundedShape
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnbShellTableTop(
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

    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground(
            modifier = Modifier
                .fillMaxSize(),
        )

        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                OnbBubbles()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            insets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        ),
                ) {
                    OnbTopAppBar(
                        step = step,
                        total = total,
                        onBack = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = externalPaddingDp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = externalPaddingDp),
                        ) {
                            art?.let { artContent ->
                                Box(
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    contentAlignment = Alignment.Center,
                                    content = { artContent() },
                                )
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )

                            if (subtitle != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.80f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .padding(horizontal = externalPaddingDp)
                    .windowInsetsPadding(
                        insets = windowInsets.only(WindowInsetsSides.Horizontal),
                    )
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
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = OnbShellDefaults.shadowElevationDp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = overlapHeightDp)
                        .padding(horizontal = 24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f),
                    ) {
                        content()
                    }
                    Spacer(Modifier.height(12.dp))
                    footer()
                    Spacer(Modifier.height(12.dp))
                    Spacer(
                        Modifier.height(
                            height = windowInsets
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
private fun OnbShellTableTopPreview() {
    DrinkWaterAppTheme {
        OnbShellTableTop(
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
        ) {
            Text("Content Here")
        }
    }
}
