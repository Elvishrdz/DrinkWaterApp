package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.settings.backup.BackupUiState
import com.eahm.drinkwaterapp.feature.settings.backup.CorruptedBackupDialog
import com.eahm.drinkwaterapp.feature.settings.backup.InitialSyncDialog
import com.eahm.drinkwaterapp.feature.settings.backup.SyncConflictSheet

@Composable
internal fun stepBackup(
    state: BackupUiState,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onConnectGoogle: () -> Unit,
    onConnectDropbox: () -> Unit,
    onResolveConflict: (useRemote: Boolean) -> Unit,
    onResolveCorruption: (overwrite: Boolean) -> Unit,
    onResolveInitialSync: (InitialSyncDecision) -> Unit,
    onDismissInitialSync: () -> Unit,
    onBack: () -> Unit,
): OnboardingStep {
    val providerName = state.providerType?.displayName.orEmpty()
    return OnboardingStep(
        art = { OnbArtCircle(icon = DwIcons.ShieldCheck) },
        title = if (state.isConnected) {
            stringResource(R.string.backup_onb_connected_title)
        } else {
            stringResource(R.string.backup_onb_title)
        },
        subtitle = if (state.isConnected) {
            stringResource(R.string.backup_onb_connected_subtitle, providerName)
        } else {
            stringResource(R.string.backup_onb_subtitle)
        },
        onBack = onBack,
        footer = {
            if (state.isConnected) {
                DwPrimaryButtonAnimated(
                    text = stringResource(R.string.backup_onb_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DwPrimaryButtonAnimated(
                    text = stringResource(R.string.backup_onb_skip),
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        content = {
            BackupResolutionDialogs(
                state = state,
                onResolveConflict = onResolveConflict,
                onResolveCorruption = onResolveCorruption,
                onResolveInitialSync = onResolveInitialSync,
                onDismissInitialSync = onDismissInitialSync,
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    // Already connected — nothing to do here; the title/subtitle confirm it.
                    state.isConnected -> Unit

                    // Connecting / first sync in progress.
                    state.isSyncing -> ConnectingIndicator()

                    else -> {
                        BackupProviderCard(
                            title = stringResource(R.string.backup_picker_google_drive),
                            onClick = onConnectGoogle,
                        )
                        if (FeatureFlags.dropboxBackupEnabled) {
                            Spacer(Modifier.height(12.dp))
                            BackupProviderCard(
                                title = stringResource(R.string.backup_picker_dropbox),
                                onClick = onConnectDropbox,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ConnectingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(color = DwWater)
        Text(
            text = stringResource(R.string.backup_onb_connecting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupResolutionDialogs(
    state: BackupUiState,
    onResolveConflict: (useRemote: Boolean) -> Unit,
    onResolveCorruption: (overwrite: Boolean) -> Unit,
    onResolveInitialSync: (InitialSyncDecision) -> Unit,
    onDismissInitialSync: () -> Unit,
) {
    state.pendingConflict?.let { conflict ->
        SyncConflictSheet(
            conflict = conflict,
            onKeepLocal = { onResolveConflict(false) },
            onUseRemote = { onResolveConflict(true) },
        )
    }
    state.pendingCorruption?.let { corruption ->
        CorruptedBackupDialog(
            corruption = corruption,
            onOverwrite = { onResolveCorruption(true) },
            onLeave = { onResolveCorruption(false) },
        )
    }
    state.pendingInitialSync?.let { remote ->
        InitialSyncDialog(
            remote = remote,
            onMerge = { onResolveInitialSync(InitialSyncDecision.MERGE) },
            onUseRemote = { onResolveInitialSync(InitialSyncDecision.USE_REMOTE) },
            onUploadLocal = { onResolveInitialSync(InitialSyncDecision.UPLOAD_LOCAL) },
            onDismiss = onDismissInitialSync,
        )
    }
}

@Composable
private fun BackupProviderCard(
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
private fun StepBackupBasePreview() {
    OnboardingStepBasePreview(
        config = stepBackup(
            state = BackupUiState(),
            onSkip = {},
            onContinue = {},
            onConnectGoogle = {},
            onConnectDropbox = {},
            onResolveConflict = {},
            onResolveCorruption = {},
            onResolveInitialSync = {},
            onDismissInitialSync = {},
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
