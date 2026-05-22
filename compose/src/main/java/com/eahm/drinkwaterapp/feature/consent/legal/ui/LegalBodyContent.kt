package com.eahm.drinkwaterapp.feature.consent.legal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

/**
 * Body content for portrait and landscape layouts — places scrollable content
 * and controls in a single column. The [ColumnScope] receiver enables
 * [ColumnScope.weight] on the policy card via [LegalScrollableContent].
 */
@Composable
internal fun ColumnScope.LegalBodyContent(
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

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun LegalBodyContentPreview() {
    DrinkWaterAppTheme {
        Column(
            Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LegalBodyContent(
                privacyToggleChecked = false,
                onToggleChanged = {},
                onAccept = {},
            )
        }
    }
}
