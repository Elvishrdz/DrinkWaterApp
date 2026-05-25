package com.eahm.drinkwaterapp.feature.drink.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.SlotPlaceholder

@Composable
internal fun DrinkLandscape(
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

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun DrinkLandscapePreview() {
    DrinkWaterAppTheme {
        DrinkLandscape(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}
