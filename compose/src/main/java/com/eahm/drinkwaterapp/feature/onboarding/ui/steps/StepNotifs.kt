@file:OptIn(ExperimentalMaterial3Api::class)

package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_MAX
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_MIN
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_STEP

/**
 * Unified notification onboarding step — two designs in one composable.
 *
 * Internal flow:
 *  - Already has permission  → Config UI directly
 *  - No permission           → Permission UI
 *      → Allow granted       → Config UI
 *      → Skip / deny         → [onSkip] (bypasses Config)
 */
@Composable
internal fun StepNotifs(
    step: Int,
    intervalMinutes: Int,
    notifAmountMl: Float,
    onAmountChange: (Float) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    // Computed once: did the user already have permission when landing on this step?
    val startedWithPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var showConfig by rememberSaveable { mutableStateOf(startedWithPermission) }
    var showDeniedDialog by rememberSaveable { mutableStateOf(false) }

    // Local stepper state — initialized from ViewModel, flushed to VM on Continue
    var localAmount by rememberSaveable {
        mutableIntStateOf(
            notifAmountMl.toInt()
                .coerceIn(AMOUNT_MIN, AMOUNT_MAX)
                .let { it - (it % AMOUNT_STEP) }
                .coerceAtLeast(AMOUNT_MIN)
        )
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) showConfig = true else showDeniedDialog = true
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showConfig = true
        }
    }

    // ── UI selection ──────────────────────────────────────────────────────────
    if (showConfig) {
        NotifsConfigContent(
            step = step,
            localAmount = localAmount,
            onDecrement = { localAmount = (localAmount - AMOUNT_STEP).coerceAtLeast(AMOUNT_MIN) },
            onIncrement = { localAmount = (localAmount + AMOUNT_STEP).coerceAtMost(AMOUNT_MAX) },
            // Back from config: go to permission UI if we came from there, else prev step
            onBack = if (startedWithPermission) onBack else ({ showConfig = false }),
            onContinue = {
                onAmountChange(localAmount.toFloat())
                onNext()
            },
        )
    } else {
        NotifsPermissionContent(
            step = step,
            intervalMinutes = intervalMinutes,
            onAllow = { requestPermission() },
            onSkip = onSkip,
            onBack = onBack,
        )
    }

    // ── Permission denied dialog ──────────────────────────────────────────────
    if (showDeniedDialog) {
        NotifsDeniedDialog(
            onGoToSettings = {
                showDeniedDialog = false
                val intent = Intent().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context.packageName)
                        putExtra("app_uid", context.applicationInfo.uid)
                    }
                }
                context.startActivity(intent)
            },
            onDismiss = {
                showDeniedDialog = false
            },
        )
    }
}

// ── Design 1: Permission ──────────────────────────────────────────────────────

@Composable
private fun NotifsPermissionContent(
    step: Int,
    intervalMinutes: Int,
    onAllow: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val intervalLabel = formatIntervalLabel(intervalMinutes)

    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_notifs_permission_title),
        subtitle = stringResource(R.string.onb_notifs_permission_subtitle, intervalLabel),
        art = { OnbArtCircle(icon = DwIcons.Bell) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.onb_notifs_permission_cta_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            DwPrimaryButton(
                text = stringResource(R.string.onb_notifs_permission_cta_allow),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        PermissionCard()
    }
}

@Composable
private fun PermissionCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "App icon",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(0.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.onb_notifs_permission_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onb_notifs_permission_card_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun NotifsConfigContent(
    step: Int,
    localAmount: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_notifs_config_title),
        subtitle = stringResource(R.string.onb_notifs_config_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Glass) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AmountStepper(
                value = localAmount,
                isAtMin = localAmount <= AMOUNT_MIN,
                isAtMax = localAmount >= AMOUNT_MAX,
                onDecrement = onDecrement,
                onIncrement = onIncrement,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onb_notifs_config_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AmountStepper(
    value: Int,
    isAtMin: Boolean,
    isAtMax: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AmountStepperButton(enabled = !isAtMin, onClick = onDecrement) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (!isAtMin) DwWater else DwWater.copy(alpha = 0.4f)),
            )
        }

        Spacer(Modifier.width(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$value",
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 56.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "ml",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = DwInk400,
            )
        }

        Spacer(Modifier.width(32.dp))

        AmountStepperButton(enabled = !isAtMax, onClick = onIncrement) {
            Icon(
                imageVector = DwIcons.Plus,
                contentDescription = "Increase",
                tint = if (!isAtMax) DwWater else DwWater.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun AmountStepperButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        content()
    }
}

// ── Denial dialog ─────────────────────────────────────────────────────────────

@Composable
private fun NotifsDeniedDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.notif_disabled_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.notif_disabled_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.action_open_settings),
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** "45m", "1h", "1h 30m" */
internal fun formatIntervalLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0 || h == 0) append("${m}m")
    }.trim()
}

// ── Previews ──────────────────────────────────────────────────────────────────

@DwPreview
@Composable
private fun StepNotifsPermissionPreview() {
    DrinkWaterAppTheme {
        NotifsPermissionContent(
            step = 6,
            intervalMinutes = 45,
            onAllow = {},
            onSkip = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepNotifsPermissionCustomIntervalPreview() {
    DrinkWaterAppTheme {
        NotifsPermissionContent(
            step = 6,
            intervalMinutes = 90,
            onAllow = {},
            onSkip = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepNotifsConfigPreview() {
    DrinkWaterAppTheme {
        NotifsConfigContent(
            step = 6,
            localAmount = 150,
            onDecrement = {},
            onIncrement = {},
            onBack = {},
            onContinue = {},
        )
    }
}

@DwPreview
@Composable
private fun StepNotifsConfigAtMinPreview() {
    DrinkWaterAppTheme {
        NotifsConfigContent(
            step = 6,
            localAmount = AMOUNT_MIN,
            onDecrement = {},
            onIncrement = {},
            onBack = {},
            onContinue = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepNotifsLandscapePreview() {
    DrinkWaterAppTheme {
        NotifsPermissionContent(
            step = 6,
            intervalMinutes = 45,
            onAllow = {},
            onSkip = {},
            onBack = {},
        )
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepNotifsTabletPreview() {
    DrinkWaterAppTheme {
        NotifsPermissionContent(
            step = 6,
            intervalMinutes = 45,
            onAllow = {},
            onSkip = {},
            onBack = {},
        )
    }
}
