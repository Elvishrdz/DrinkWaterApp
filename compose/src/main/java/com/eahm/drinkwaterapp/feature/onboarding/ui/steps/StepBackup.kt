package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

@Composable
internal fun stepBackup(
    onSkip: () -> Unit,
    onConnectGoogle: () -> Unit,
    onConnectDropbox: () -> Unit,
    onBack: () -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.ShieldCheck) },
    title = stringResource(R.string.backup_onb_title),
    subtitle = stringResource(R.string.backup_onb_subtitle),
    onBack = onBack,
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.backup_onb_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BackupProviderCard(
                icon = { ProviderIcon(tint = Color(0xFF4285F4)) },
                title = stringResource(R.string.backup_picker_google_drive),
                onClick = onConnectGoogle,
            )
            Spacer(Modifier.height(12.dp))
            BackupProviderCard(
                icon = { ProviderIcon(tint = Color(0xFF0061FF)) },
                title = stringResource(R.string.backup_picker_dropbox),
                onClick = onConnectDropbox,
            )
        }
    },
)

@Composable
private fun BackupProviderCard(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
) {
    DwPrimaryButtonAnimated(
        text = title,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProviderIcon(tint: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DwTintWater),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = DwIcons.ShieldCheck,
            contentDescription = null,
            tint = DwWater,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun StepBackupBasePreview() {
    OnboardingStepBasePreview(
        config = stepBackup(
            onSkip = {},
            onConnectGoogle = {},
            onConnectDropbox = {},
            onBack = {},
        ),
    )
}

@DwPreview
@Composable
private fun StepBackupPreview() {
    DrinkWaterAppTheme {
        StepBackupBasePreview()
    }
}
