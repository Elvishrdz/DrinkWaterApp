package com.eahm.drinkwaterapp.feature.consent.legal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.extension.sanitizeHtml

/**
 * Scrollable part of the body: lede text + policy card + web link.
 * [ColumnScope] receiver enables [ColumnScope.weight] on the policy card.
 */
@Composable
internal fun LegalScrollableContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.consent_legal_lede),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Scrollable policy card — expands to fill available vertical space
        Surface(
            modifier = Modifier,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    //.verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                val localContent = stringResource(R.string.privacy_policy_local_content)
                val styledText = AnnotatedString.fromHtml(localContent.sanitizeHtml())
                Text(
                    text = styledText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 500)
@Composable
private fun LegalScrollableContentPreview() {
    DrinkWaterAppTheme {
        LegalScrollableContent()
    }
}
