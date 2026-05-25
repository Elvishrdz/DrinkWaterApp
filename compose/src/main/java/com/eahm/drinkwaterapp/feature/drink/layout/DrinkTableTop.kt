package com.eahm.drinkwaterapp.feature.drink.layout

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.SlotPlaceholder

@Composable
internal fun DrinkTableTop(
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

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun DrinkTableTopPreview() {
    DrinkWaterAppTheme {
        DrinkTableTop(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}
