package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.eahm.drinkwaterapp.core.presentation.layouttype.NavigationType
import com.eahm.drinkwaterapp.core.presentation.layouttype.getLayoutAndNavigationType
import com.eahm.drinkwaterapp.feature.drink.layout.DrinkLandscape
import com.eahm.drinkwaterapp.feature.drink.layout.DrinkPortrait
import com.eahm.drinkwaterapp.feature.drink.layout.DrinkTableTop
import com.eahm.drinkwaterapp.feature.drink.ui.ActionsSection
import com.eahm.drinkwaterapp.feature.drink.ui.InfoSection
import com.eahm.drinkwaterapp.feature.drink.ui.RingSection
import com.eahm.drinkwaterapp.feature.drink.ui.previewLogEntries

@Composable
internal fun DrinkScreen(
    state: DrinkState.Data,
    onAction: (DrinkAction) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val windowInsets = WindowInsets.safeDrawing.union(WindowInsets.displayCutout)

    val innerPadding = PaddingValues()
    val (layoutType, navigationType) = getLayoutAndNavigationType()

    val ringSection: @Composable () -> Unit = {
        RingSection(state = state)
    }

    val actionsSection: @Composable () -> Unit = {
        ActionsSection(
            state = state,
            onAction = onAction,
        )
    }

    val infoSection: @Composable () -> Unit = {
        InfoSection(
            state = state,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
        )
    }

    // only calculate bottom padding when using navigation rail and no banner visible
    val bottomPadding =
        if (navigationType == NavigationType.NavigationRail && state.isAdFree) {
            windowInsets.asPaddingValues().calculateBottomPadding()
        } else {
            0.dp
        }

    val contentPadding = PaddingValues(
        top = windowInsets.asPaddingValues().calculateTopPadding(),
        bottom = bottomPadding,
    )

    // TODO: persist drink water presets suggestions bottom sheet and value when changing layouts
    when (layoutType) {
        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.PORTRAIT,
            -> DrinkPortrait(
            state = state,
            modifier = Modifier
                .windowInsetsPadding(
                    insets = windowInsets.only(WindowInsetsSides.Horizontal),
                )
                .fillMaxSize(),
            contentPadding = contentPadding,
            onAction = onAction,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED,
            -> {
            val insets = when (navigationType) {
                NavigationType.NavigationBar -> windowInsets
                    .only(WindowInsetsSides.Horizontal)

                NavigationType.NavigationRail -> windowInsets
                    .only(WindowInsetsSides.End)
            }

            DrinkLandscape(
                state = state,
                modifier = Modifier
                    .windowInsetsPadding(insets)
                    .fillMaxSize(),
                contentPadding = contentPadding,
                onAction = onAction,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
            )
        }

        LayoutType.TABLE_TOP,
            -> DrinkTableTop(
            innerPadding = innerPadding,
            modifier = Modifier,
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
            state = drinkStateMock,
            onAction = {},
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
            state = drinkStateMock,
            onAction = {},
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
            state = drinkStateMock,
            onAction = {},
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
            state = drinkStateMock.copy(
                drinkStats = DrinkStats(
                    goalAmount = 2400f,
                    drinkAmount = 2400f,
                    goalReached = true
                ),
                drinkStreakDays = 7,
                showStreakBadge = true,
            ),
            onAction = {},
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

internal val drinkStateMock = DrinkState.Data(
    isAdFree = false,
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
