package com.eahm.drinkwaterapp.feature.onboarding.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

@Composable
internal fun SkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.onb_overlay_request_cta_skip),
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun SkipButtonPreview() {
    DrinkWaterAppTheme {
        SkipButton(
            onClick = {},
        )
    }
}
