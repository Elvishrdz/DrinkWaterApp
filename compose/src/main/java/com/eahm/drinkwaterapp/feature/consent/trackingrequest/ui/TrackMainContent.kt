package com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

/** Lede text + feature card. Shared across all layout variants. */
@Composable
internal fun TrackMainContent() {
    Text(
        text = stringResource(R.string.consent_track_lede),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            FeatureCardRow(
                imageVector = DwIcons.Lightbulb,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                title = stringResource(R.string.consent_track_feature_usage_title),
                description = stringResource(R.string.consent_track_feature_usage_desc),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            FeatureCardRow(
                imageVector = DwIcons.Star,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                title = stringResource(R.string.consent_track_feature_private_title),
                description = stringResource(R.string.consent_track_feature_private_desc),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            FeatureCardRow(
                imageVector = DwIcons.Settings,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                title = stringResource(R.string.consent_track_feature_control_title),
                description = stringResource(R.string.consent_track_feature_control_desc),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TrackMainContentPreview() {
    DrinkWaterAppTheme {
        Column(Modifier.padding(16.dp)) {
            TrackMainContent()
        }
    }
}
