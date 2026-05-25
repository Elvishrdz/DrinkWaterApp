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
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.getLayoutAndNavigationType
import com.eahm.drinkwaterapp.feature.drink.layout.PortraitLayout
import com.eahm.drinkwaterapp.feature.drink.ui.ActionsSection
import com.eahm.drinkwaterapp.feature.drink.ui.InfoSection
import com.eahm.drinkwaterapp.feature.drink.ui.RingSection
import com.eahm.drinkwaterapp.feature.drink.ui.previewLogEntries

@Composable
internal fun DrinkScreen(
    state: DrinkState.Data,
    onDrink: (Float, DrinkSource) -> Unit,
    onReorderPresets: (List<DrinkSuggestion>, Boolean) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onDeletePreset: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    onBottomSheetOpened: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val innerPadding = PaddingValues()
    val (layoutType, navigation) = getLayoutAndNavigationType()

    val ringSection: @Composable () -> Unit = {
        RingSection(state = state)
    }

    val actionsSection: @Composable () -> Unit = {
        ActionsSection(
            state = state,
            onDrink = onDrink,
            onReorderPresets = onReorderPresets,
            onCreatePreset = onCreatePreset,
            onDeletePreset = onDeletePreset,
            onRestorePreset = onRestorePreset,
            onBottomSheetOpened = onBottomSheetOpened,
        )
    }

    val infoSection: @Composable () -> Unit = {
        InfoSection(
            state = state,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
        )
    }

    /*
    *  when using navigation rail AND no banner we should calculate bottom padding
    *  when using navigation rail AND banner we should not calculate bottom padding
    *
    * */

    when (layoutType) {
        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.PORTRAIT,
            -> PortraitLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED,
            -> LandscapeLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )

        LayoutType.TABLE_TOP,
            -> TableTopLayout(
            innerPadding = innerPadding,
            modifier = modifier,
            ringSection = ringSection,
            actionsSection = actionsSection,
            infoSection = infoSection,
        )
    }
}

//@Preview
//@Composable
//private fun DrinkScreenPreview(
//    @PreviewParameter(LayoutTypesPreviewProvider::class) layoutType: LayoutType,
//) {
//    DrinkWaterAppTheme {
//        DrinkScreen(
//            layoutType = layoutType,
//        )
//    }
//}

@Preview(name = "Portrait")
@Composable
private fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = { _, _ -> },
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onBottomSheetOpened = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun DrinkScreenLandscapePreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = { _, _ -> },
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onBottomSheetOpened = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun DrinkScreenTabletPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = { _, _ -> },
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onBottomSheetOpened = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview(name = "Goal reached")
@Composable
private fun DrinkScreenGoalReachedPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock.copy(
                drinkStats = DrinkStats(
                    goalAmount = 2400f,
                    drinkAmount = 2400f,
                    goalReached = true
                ),
                drinkStreakDays = 7,
                showStreakBadge = true,
            ),
            onDrink = { _, _ -> },
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onBottomSheetOpened = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}

internal val drinkSuggestionsMocks = listOf(
    DrinkSuggestion(1, 100f),
    DrinkSuggestion(2, 200f),
    DrinkSuggestion(3, 300f),
)

private val stateMock = DrinkState.Data(
    drinkStreakDays = 0,
    showStreakBadge = false,
    drinkStats = DrinkStats(
        goalAmount = 2400f,
        drinkAmount = 1500f,
        goalReached = false,
    ),
    quickDrinkSuggestions = drinkSuggestionsMocks.take(3),
    allDrinkSuggestions = drinkSuggestionsMocks,
    todayLogs = previewLogEntries.take(4),
    alarmCountdownMs = null,
)

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
internal fun SlotPlaceholder(label: String, color: Color, modifier: Modifier = Modifier) {
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
