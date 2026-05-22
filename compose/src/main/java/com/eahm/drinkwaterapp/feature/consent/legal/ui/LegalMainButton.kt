package com.eahm.drinkwaterapp.feature.consent.legal.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton

@Composable
internal fun LegalMainButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    DwPrimaryButton(
        text = stringResource(R.string.consent_legal_cta),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    )
}
