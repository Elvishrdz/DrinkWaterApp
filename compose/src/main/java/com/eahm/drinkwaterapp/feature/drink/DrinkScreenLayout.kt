package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

/**
 * Adaptive layout for the drink screen. Arranges [ringSection], [actionsSection], and
 * [infoSection] differently depending on the current [LayoutType]:
 *
 * - [LayoutType.PORTRAIT]: single scrollable column stacked vertically.
 * - [LayoutType.LANDSCAPE] / [LayoutType.FOLDABLE_EXPANDED]: two equal columns — ring on the
 *   left, actions + info on the right, each independently scrollable.
 * - [LayoutType.TABLE_TOP]: ring fills the top half (centered), actions and info split the
 *   bottom half side-by-side, each independently scrollable.
 */
@Composable
internal fun DrinkLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    ringSection: @Composable () -> Unit,
    actionsSection: @Composable () -> Unit,
    infoSection: @Composable () -> Unit,
) {
    when (getLayoutType()) {
        LayoutType.PORTRAIT -> PortraitLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED -> LandscapeLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )

        LayoutType.TABLE_TOP -> TableTopLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )
    }
}

@Composable
private fun PortraitLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    ringSection: @Composable () -> Unit,
    actionsSection: @Composable () -> Unit,
    infoSection: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        ringSection()
        Spacer(Modifier.height(20.dp))
        actionsSection()
        Spacer(Modifier.height(16.dp))
        infoSection()
        Spacer(Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
    }
}

@Composable
private fun LandscapeLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    ringSection: @Composable () -> Unit,
    actionsSection: @Composable () -> Unit,
    infoSection: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Left column: ring centered vertically
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ringSection()
        }

        // Right column: actions then info, scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            actionsSection()
            Spacer(Modifier.height(16.dp))
            infoSection()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TableTopLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    ringSection: @Composable () -> Unit,
    actionsSection: @Composable () -> Unit,
    infoSection: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Top half: ring centered
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ringSection()
        }

        // Bottom half: actions (left) and info (right), side-by-side
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                actionsSection()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(8.dp))
                infoSection()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SlotPlaceholder(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Preview(name = "Portrait")
@Composable
private fun PortraitLayoutPreview() {
    DrinkWaterAppTheme {
        PortraitLayout(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun LandscapeLayoutPreview() {
    DrinkWaterAppTheme {
        LandscapeLayout(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun TableTopLayoutPreview() {
    DrinkWaterAppTheme {
        TableTopLayout(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}
