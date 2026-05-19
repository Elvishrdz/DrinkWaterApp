package com.eahm.drinkwaterapp.feature.consent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

private val LegalWaveHeight = 248.dp
private val LegalOverlapHeight = 25.dp

@Composable
internal fun LegalScreen(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val innerPadding = PaddingValues()
    when (getLayoutType()) {
        LayoutType.PORTRAIT -> LegalPortraitLayout(
            innerPadding = innerPadding,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_EXPANDED -> LegalLandscapeLayout(
            innerPadding = innerPadding,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.TABLE_TOP -> LegalTableTopLayout(
            innerPadding = innerPadding,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )
    }
}

// region Layouts

/**
 * Portrait: wave header at the top, overlapping white sheet at the bottom.
 * Identical to the original single-column design.
 */
@Composable
private fun LegalPortraitLayout(
    innerPadding: PaddingValues,
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // TODO: show background over the status bar
        val sheetHeight = maxHeight - (LegalWaveHeight - LegalOverlapHeight)

        // Wave header
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(LegalWaveHeight)
                .zIndex(0f)
                .background(waterGradient),
        ) {
            LegalBubbles()
            Column(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 40.dp),
            ) {
                LegalHeaderContent(showStepCounter = showStepCounter, centered = false)
            }
        }

        // Overlapping white sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .align(Alignment.BottomStart)
                .zIndex(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LegalBodyContent(
                    privacyToggleChecked = privacyToggleChecked,
                    onToggleChanged = onToggleChanged,
                    onAccept = onAccept,
                )
            }
        }
    }
}

/**
 * Landscape / foldable-expanded: gradient branding panel on the left,
 * white content surface on the right.
 */
@Composable
private fun LegalLandscapeLayout(
    innerPadding: PaddingValues,
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Left: gradient branding panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(waterGradient),
        ) {
            LegalBubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LegalHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(22.dp)
                .background(color = MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LegalBodyContent(
                privacyToggleChecked = privacyToggleChecked,
                onToggleChanged = onToggleChanged,
                onAccept = onAccept,
            )
        }
    }
}

/**
 * Table-top: gradient branding panel on the top half,
 * white content surface on the bottom half.
 */
@Composable
private fun LegalTableTopLayout(
    innerPadding: PaddingValues,
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    val waterGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Top: gradient branding panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(waterGradient),
        ) {
            LegalBubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LegalHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        // Bottom: white surface — left scrollable content, right controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset(0.dp, (-25).dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
                    .padding(top = 25.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Left: scrollable policy content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    LegalScrollableContent()
                }

                // Right: agree row + CTA, centered vertically
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                ) {
                    LegalControls(
                        privacyToggleChecked = privacyToggleChecked,
                        onToggleChanged = onToggleChanged,
                        onAccept = onAccept,
                    )
                }
            }
        }
    }
}

// endregion

// region Shared composables

@Composable
private fun LegalBubbles() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-20).dp)
                .background(Color.White.copy(alpha = 0.14f), CircleShape),
        )
        Box(
            Modifier
                .size(90.dp)
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 60.dp)
                .background(Color.White.copy(alpha = 0.09f), CircleShape),
        )
        Box(
            Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-60).dp, y = 10.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
        )
    }
}

@Composable
private fun LegalHeaderContent(
    showStepCounter: Boolean,
    centered: Boolean,
) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start

    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.30f),
                RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = DwIcons.ShieldCheck,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }

    Spacer(Modifier.height(16.dp))

    if (showStepCounter) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(30.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White),
            )
            Box(
                Modifier
                    .width(22.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.30f)),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.consent_legal_step),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.em,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
    } else {
        Spacer(Modifier.height(16.dp))
    }

    Text(
        text = stringResource(R.string.consent_legal_title),
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.consent_legal_updated),
        color = Color.White.copy(alpha = 0.80f),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 0.04.em,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Scrollable part of the body: lede text + policy card + web link.
 * [ColumnScope] receiver enables [ColumnScope.weight] on the policy card.
 */
@Composable
private fun ColumnScope.LegalScrollableContent() {
    val uriHandler = LocalUriHandler.current

    // Lede
    Text(
        text = stringResource(R.string.consent_legal_lede),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    // Scrollable policy card — expands to fill available vertical space
    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.privacy_policy_local_content),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    // Web link
    Row(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .clickable { uriHandler.openUri("https://www.freeprivacypolicy.com/privacy/view/04c015dc119a00b1ec8a21df46a9bfb1") }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = DwIcons.Share,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.consent_legal_web_link),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.04.em,
        )
    }
}

/**
 * Controls: agree row + CTA button.
 */
@Composable
private fun LegalControls(
    privacyToggleChecked: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    // Agree row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onToggleChanged(!privacyToggleChecked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(
            checked = privacyToggleChecked,
            onCheckedChange = onToggleChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        Text(
            text = stringResource(R.string.consent_legal_agree),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }

    DwPrimaryButton(
        text = stringResource(R.string.consent_legal_cta),
        onClick = onAccept,
        enabled = privacyToggleChecked,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Body content for portrait and landscape layouts — places scrollable content
 * and controls in a single column. The [ColumnScope] receiver enables
 * [ColumnScope.weight] on the policy card via [LegalScrollableContent].
 */
@Composable
private fun ColumnScope.LegalBodyContent(
    privacyToggleChecked: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    LegalScrollableContent()
    LegalControls(
        privacyToggleChecked = privacyToggleChecked,
        onToggleChanged = onToggleChanged,
        onAccept = onAccept,
    )
}

// endregion

// region Previews

@Preview(name = "Portrait")
@Composable
private fun LegalScreenPortraitPreview() {
    DrinkWaterAppTheme {
        LegalScreen(
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun LegalScreenLandscapePreview() {
    DrinkWaterAppTheme {
        LegalLandscapeLayout(
            innerPadding = PaddingValues(0.dp),
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun LegalScreenTableTopPreview() {
    DrinkWaterAppTheme {
        LegalTableTopLayout(
            innerPadding = PaddingValues(0.dp),
            privacyToggleChecked = false,
            showStepCounter = true,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}

// endregion
