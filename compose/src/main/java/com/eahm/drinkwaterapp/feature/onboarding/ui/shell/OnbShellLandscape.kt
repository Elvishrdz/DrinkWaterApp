package com.eahm.drinkwaterapp.feature.onboarding.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
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
    weight: Float,
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
                .weight(weight)
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

                Spacer(modifier = Modifier.height(3.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val containerHeight = constraints.maxHeight
                    val density = LocalDensity.current
                    val artGapPx = with(density) { 10.dp.roundToPx() }
                    val stackTopPaddingPx = with(density) { contentPaddingDp.roundToPx() }
                    val bottomInsetDp = windowInsets.only(WindowInsetsSides.Bottom)
                        .asPaddingValues().calculateBottomPadding()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = externalPaddingDp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Layout(
                            content = {
                                Box { art?.invoke() }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { measurables, lc ->
                            val artPlaceable =
                                measurables[0].measure(lc.copy(minWidth = 0, minHeight = 0))
                            val titlePlaceable = measurables[1].measure(lc.copy(minHeight = 0))
                            val hasArt = art != null && artPlaceable.height > 0
                            val artH = artPlaceable.height

                            val artY: Int
                            val titleY: Int
                            val layoutH: Int

                            if (hasArt) {
                                val proposedTitleY = (containerHeight - titlePlaceable.height) / 2
                                val proposedArtY = proposedTitleY - artGapPx - artH
                                if (proposedArtY >= 0) {
                                    // Fits: title centered, art directly above
                                    artY = proposedArtY
                                    titleY = proposedTitleY
                                    layoutH = containerHeight
                                } else {
                                    // Doesn't fit centered: stack, let scroll take over
                                    artY = stackTopPaddingPx
                                    titleY = artY + artH + artGapPx
                                    layoutH = titleY + titlePlaceable.height
                                }
                            } else {
                                // No art: center title
                                artY = 0
                                titleY = (containerHeight - titlePlaceable.height) / 2
                                layoutH = containerHeight
                            }

                            layout(lc.maxWidth, layoutH) {
                                if (hasArt) {
                                    artPlaceable.placeRelative(
                                        x = (lc.maxWidth - artPlaceable.width) / 2,
                                        y = artY,
                                    )
                                }
                                titlePlaceable.placeRelative(
                                    x = (lc.maxWidth - titlePlaceable.width) / 2,
                                    y = titleY,
                                )
                            }
                        }

                        Spacer(Modifier.height(bottomInsetDp))
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

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun OnbShellLandscapePreview() {
    DrinkWaterAppTheme {
        OnbShellLandscape(
            weight = 1f,
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
