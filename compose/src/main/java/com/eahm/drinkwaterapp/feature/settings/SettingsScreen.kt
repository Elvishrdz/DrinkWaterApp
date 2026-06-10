package com.eahm.drinkwaterapp.feature.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.core.billing.PurchaseState
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwDanger
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintGold
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.domain.extension.sanitizeZeroDecimals
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.core.presentation.composable.BuildInfoFooter
import com.eahm.drinkwaterapp.feature.reminders.components.DaysOfWeekToggle
import com.eahm.drinkwaterapp.feature.settings.age.AgeSettingsSection
import com.eahm.drinkwaterapp.feature.settings.backup.BackupSettingsSection
import com.eahm.drinkwaterapp.feature.settings.backup.ProviderPickerSheet
import com.eahm.drinkwaterapp.feature.settings.extension.openPlayStoreUrl
import com.eahm.drinkwaterapp.feature.settings.extension.openPrivacyPolicyAndTermsOfUse
import com.eahm.drinkwaterapp.feature.settings.extension.startAppShareIntent
import com.eahm.drinkwaterapp.feature.settings.extension.startFeatureRequestIntent
import com.eahm.drinkwaterapp.feature.settings.extension.startReportProblemIntent
import com.eahm.drinkwaterapp.feature.settings.ui.EditButton
import com.eahm.drinkwaterapp.feature.settings.ui.GoalBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.IntervalBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.LanguageDialog
import com.eahm.drinkwaterapp.feature.settings.ui.NotificationAmountBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.RemoveAdsBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.TimeFormatBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.UnitPreferenceBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.UnitsBottomSheet
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorDialog
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

private val gradientMagentaHero = Brush.verticalGradient(
    colors = listOf(Color(0xFFB83A95), DwMagentaDark)
)

@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val trackingService = koinInject<TrackingService>()

    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    var showRevokeTrackingDialog by rememberSaveable { mutableStateOf(false) }

    // ── Remove Ads purchase sheet ─────────────────────────────────────────────
    var showPurchaseSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreSuccessDialog by rememberSaveable { mutableStateOf(false) }
    val activity = context as Activity

    LaunchedEffect(state.purchaseState) {
        if (state.offeringsState !is OfferingsState.Success) return@LaunchedEffect
        when (state.purchaseState) {
            is PurchaseState.Success -> {
                viewModel.onOfferingsSheetDismissed()
                showPurchaseSuccessDialog = true
            }

            is PurchaseState.Restored -> {
                viewModel.onOfferingsSheetDismissed()
                showRestoreSuccessDialog = true
            }

            else -> Unit
        }
    }

    // ── Notification permission ───────────────────────────────────────────────
    var showPermissionDeniedDialog by rememberSaveable { mutableStateOf(false) }
    var showDisableNotificationsDialog by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            viewModel.onNotificationsToggled(true)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    fun handleNotificationToggle(enabled: Boolean) {
        if (!enabled) {
            showDisableNotificationsDialog = true
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                viewModel.onNotificationsToggled(true)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // No runtime permission needed below Android 13
            viewModel.onNotificationsToggled(true)
        }
    }

    // ── Draw-overlay permission ───────────────────────────────────────────────
    var showOverlayInstructionsDialog by rememberSaveable { mutableStateOf(false) }

    val drawOverlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onDrawOverlayPermissionResult()
    }

    fun handleDrawOverlayToggle() {
        showOverlayInstructionsDialog = true
    }

    // ── Exact alarm permission ────────────────────────────────────────────────
    var showExactAlarmInstructionsDialog by rememberSaveable { mutableStateOf(false) }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onExactAlarmPermissionResult()
    }

    fun handleExactAlarmTap() {
        showExactAlarmInstructionsDialog = true
    }

    // ── Google Sign-In for Backup ─────────────────────────────────────────────
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.onGoogleSignInResult(result.data)
    }

    // Dropbox result arrives via onResume after the external browser flow
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onDropboxResume()
    }

    SettingsContent(
        state = state,
        onNotificationsToggled = ::handleNotificationToggle,
        onUsernameChanged = viewModel::onUsernameChanged,
        onGoalChanged = viewModel::onGoalChanged,
        onUnitChanged = viewModel::onUnitChanged,
        onIntervalChanged = viewModel::onIntervalChanged,
        onNotificationAmountChanged = viewModel::onNotificationAmountChanged,
        onStartTimeChanged = viewModel::onStartTimeChanged,
        onEndTimeChanged = viewModel::onEndTimeChanged,
        onShareTap = {
            trackingService.trackSettingsShareTapped()
            context.startAppShareIntent()
        },
        onRateTap = {
            trackingService.trackSettingsRateAppTapped()
            context.openPlayStoreUrl()
        },
        onRequestFeatureTap = {
            trackingService.trackSettingsFeatureRequestTapped()
            context.startFeatureRequestIntent()
        },
        onReportProblemTap = {
            trackingService.trackSettingsReportProblemTapped()
            context.startReportProblemIntent()
        },
        onGoalCelebrationsToggled = { handleDrawOverlayToggle() },
        onExactAlarmTap = ::handleExactAlarmTap,
        onClearDataTap = { showClearDataDialog = true },
        onRemoveAdsTap = {
            trackingService.trackSettingsRemoveAdsTapped()
            viewModel.onFetchOfferings()
        },
        onTimeFormatChanged = viewModel::onTimeFormatPreferenceChanged,
        onUnitPreferenceChanged = viewModel::onUnitPreferenceChanged,
        onLanguageChanged = viewModel::onLanguageChanged,
        onTrackingToggled = { enabled ->
            if (enabled) viewModel.onTrackingToggled(true)
            else showRevokeTrackingDialog = true
        },
        onPrivacyPolicyAndTermsOfUseTap = context::openPrivacyPolicyAndTermsOfUse,
        onConnectBackup = { provider ->
            when (provider) {
                CloudProviderType.GOOGLE_DRIVE -> {
                    coroutineScope.launch {
                        val sender = viewModel.getGoogleAuthorizationIntentSender()
                        if (sender != null) {
                            googleSignInLauncher.launch(IntentSenderRequest.Builder(sender).build())
                        } else {
                            viewModel.onConnectBackup(CloudProviderType.GOOGLE_DRIVE)
                        }
                    }
                }

                CloudProviderType.DROPBOX -> viewModel.startDropboxOAuth()
            }
        },
        onSyncNow = viewModel::onSyncNow,
        onDisconnectBackupTap = viewModel::onDisconnectBackup,
        onRestoreBackupTap = viewModel::onRestoreBackup,
        onSyncOnSaveToggled = viewModel::onSyncOnSaveToggled,
        onResolveConflict = viewModel::onResolveConflict,
        onResolveCorruption = viewModel::onResolveCorruption,
        onResolveInitialSync = viewModel::onResolveInitialSync,
        onDismissInitialSync = viewModel::onDismissInitialSync,
        onAutoBackupHourChanged = viewModel::onAutoBackupHourChanged,
        onBackupIntervalChanged = viewModel::onBackupIntervalChanged,
        onActiveDayToggled = viewModel::onActiveDayToggled,
        onEnableAllDays = viewModel::onEnableAllDays,
        modifier = modifier,
    )

    // ── Permission denied dialog ──────────────────────────────────────────────
    if (showPermissionDeniedDialog) {
        NotificationPermissionDeniedDialog(
            onDismiss = { showPermissionDeniedDialog = false },
            onGoToSettings = {
                showPermissionDeniedDialog = false

                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

                } else {
                    // TODO: test this before release
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", context.packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                }

                context.startActivity(intent)
            },
        )
    }

    // ── Disable notifications dialog ──────────────────────────────────────────
    if (showDisableNotificationsDialog) {
        DisableNotificationsDialog(
            onDismiss = { showDisableNotificationsDialog = false },
            onGoToSettings = {
                showDisableNotificationsDialog = false
                trackingService.trackSettingsNotificationsDisabled()

                val intent = Intent()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", context.packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                }

                context.startActivity(intent)
            },
        )
    }

    // ── Clear all data dialog ─────────────────────────────────────────────────
    if (showClearDataDialog) {
        ClearAllDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                showClearDataDialog = false
                trackingService.trackSettingsResetAllDataConfirmed()
                viewModel.clearAllData()
            },
        )
    }

    // ── Revoke tracking dialog ────────────────────────────────────────────────
    if (showRevokeTrackingDialog) {
        RevokeTrackingDialog(
            onDismiss = { showRevokeTrackingDialog = false },
            onConfirm = {
                showRevokeTrackingDialog = false
                viewModel.onTrackingToggled(false)
            },
        )
    }

    // ── Draw-overlay instructions dialog ─────────────────────────────────────
    if (showOverlayInstructionsDialog) {
        DrawOverlayInstructionsDialog(
            onDismiss = { showOverlayInstructionsDialog = false },
            onGoToSettings = {
                showOverlayInstructionsDialog = false
                trackingService.trackSettingsGoalReachedOpened()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
                drawOverlayLauncher.launch(intent)
            },
        )
    }

    // ── Exact alarm instructions dialog ───────────────────────────────────────
    if (showExactAlarmInstructionsDialog) {
        ExactAlarmInstructionsDialog(
            isGranted = state.exactAlarmsEnabled,
            onDismiss = { showExactAlarmInstructionsDialog = false },
            onGoToSettings = {
                showExactAlarmInstructionsDialog = false
                trackingService.trackSettingsReminderTypeOpened()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    exactAlarmLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    )
                }
            },
        )
    }

    // ── Remove Ads bottom sheet ───────────────────────────────────────────────
    state.offeringsState?.let { offeringsState ->
        RemoveAdsBottomSheet(
            offeringsState = offeringsState,
            purchaseState = state.purchaseState,
            onPurchase = {
                val pkg = (offeringsState as? OfferingsState.Success)?.pkg
                if (pkg != null) viewModel.onPurchase(activity, pkg)
            },
            onRestore = viewModel::onRestorePurchase,
            onRetry = { viewModel.onFetchOfferings() },
            onDismiss = { viewModel.onOfferingsSheetDismissed() },
        )
    }

    // ── Purchase success dialog ───────────────────────────────────────────────
    if (showPurchaseSuccessDialog) {
        PurchaseSuccessDialog(onDismiss = { showPurchaseSuccessDialog = false })
    }

    // ── Restore success dialog ────────────────────────────────────────────────
    if (showRestoreSuccessDialog) {
        RestoreSuccessDialog(onDismiss = { showRestoreSuccessDialog = false })
    }
}

// ── Content composable ────────────────────────────────────────────────────────

@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
@Composable
internal fun SettingsContent(
    state: SettingsState,
    onNotificationsToggled: (Boolean) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onGoalChanged: (Float) -> Unit,
    onUnitChanged: (WaterUnit, WeightUnit) -> Unit,
    onIntervalChanged: (Int) -> Unit,
    onNotificationAmountChanged: (Float) -> Unit,
    onStartTimeChanged: (TimeSelected) -> Unit,
    onEndTimeChanged: (TimeSelected) -> Unit,
    onShareTap: () -> Unit,
    onRateTap: () -> Unit,
    onRequestFeatureTap: () -> Unit,
    onReportProblemTap: () -> Unit,
    onGoalCelebrationsToggled: (Boolean) -> Unit,
    onExactAlarmTap: () -> Unit,
    onClearDataTap: () -> Unit,
    onRemoveAdsTap: () -> Unit,
    onTimeFormatChanged: (TimeFormatPreference) -> Unit,
    onUnitPreferenceChanged: (UnitPreference) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onTrackingToggled: (Boolean) -> Unit,
    onPrivacyPolicyAndTermsOfUseTap: () -> Unit,
    onConnectBackup: (CloudProviderType) -> Unit = {},
    onSyncNow: () -> Unit = {},
    onDisconnectBackupTap: () -> Unit = {},
    onRestoreBackupTap: () -> Unit = {},
    onSyncOnSaveToggled: (Boolean) -> Unit = {},
    onResolveConflict: (useRemote: Boolean) -> Unit = {},
    onResolveCorruption: (overwrite: Boolean) -> Unit = {},
    onResolveInitialSync: (InitialSyncDecision) -> Unit = {},
    onDismissInitialSync: () -> Unit = {},
    onAutoBackupHourChanged: (Int) -> Unit = {},
    onBackupIntervalChanged: (Int) -> Unit = {},
    onActiveDayToggled: (Int) -> Unit = {},
    onEnableAllDays: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val is24Hour = when (state.timeFormatPreference) {
        TimeFormatPreference.SYSTEM -> DateFormat.is24HourFormat(context)
        TimeFormatPreference.HOUR_12 -> false
        TimeFormatPreference.HOUR_24 -> true
    }
    // Window duration used to cap the reminder interval
    val windowMinutes = remember(state.startTime, state.endTime) {
        val s = state.startTime ?: return@remember 24 * 60
        val e = state.endTime ?: return@remember 24 * 60
        val startMin = s.hour * 60 + s.minute
        val endMin = e.hour * 60 + e.minute
        if (endMin > startMin) endMin - startMin else (24 * 60 - startMin) + endMin
    }

    // Bottom-sheet visibility flags
    var showGoalSheet by rememberSaveable { mutableStateOf(false) }
    var showUnitsSheet by rememberSaveable { mutableStateOf(false) }
    var showIntervalSheet by rememberSaveable { mutableStateOf(false) }
    var showNotificationAmountSheet by rememberSaveable { mutableStateOf(false) }
    var showTimeFormatSheet by rememberSaveable { mutableStateOf(false) }
    var showUnitSheet by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

//            item {
//                ProfileCard(
//                    state = state,
//                    onUsernameChanged = onUsernameChanged,
//                )
//            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_hydration)) {
                    SettingRow(
                        icon = DwIcons.Drop,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.label_daily_goal),
                        onClick = { showGoalSheet = true },
                    ) {
                        RowValueTrailing(value = state.dailyGoalMl.formatVolume(state.unitPreference))
                    }
                    if (false) {
                        // TODO: setup units in the app
                        HorizontalDivider(color = DwInk100)
                        SettingRow(
                            icon = DwIcons.Ruler,
                            iconBg = DwTintMagenta,
                            iconTint = DwMagenta,
                            label = stringResource(R.string.settings_row_units),
                            onClick = { showUnitsSheet = true },
                        ) {
                            RowValueTrailing(value = "${state.unit.label} · ${state.weightUnit.label}")
                        }
                    }
                }
            }

            if (!state.isAdFree) {
                item {
                    SettingsSection(title = stringResource(R.string.settings_section_premium)) {
                        SettingRow(
                            icon = DwIcons.Star,
                            iconBg = DwTintGold,
                            iconTint = Color.White,
                            label = stringResource(R.string.settings_row_remove_ads),
                            onClick = onRemoveAdsTap,
                        ) {
                            RowChevronTrailing()
                        }
                    }
                }
            }

            item {
                AgeSettingsSection()
            }

            if (FeatureFlags.cloudBackupEnabled) {
                item {
                    var showProviderPickerSheet by rememberSaveable { mutableStateOf(false) }
                    var showDisconnectBackupDialog by rememberSaveable { mutableStateOf(false) }
                    var showRestoreBackupDialog by rememberSaveable { mutableStateOf(false) }

                    BackupSettingsSection(
                        state = state.backupUiState,
                        onConnect = { showProviderPickerSheet = true },
                        onSyncNow = onSyncNow,
                        onRestore = { showRestoreBackupDialog = true },
                        onDisconnect = { showDisconnectBackupDialog = true },
                        onSyncOnSaveToggled = onSyncOnSaveToggled,
                        onResolveConflict = onResolveConflict,
                        onResolveCorruption = onResolveCorruption,
                        onResolveInitialSync = onResolveInitialSync,
                        onDismissInitialSync = onDismissInitialSync,
                        onAutoBackupHourChanged = onAutoBackupHourChanged,
                        onBackupIntervalChanged = onBackupIntervalChanged,
                    )

                    if (showProviderPickerSheet) {
                        ProviderPickerSheet(
                            onProviderSelected = { provider ->
                                showProviderPickerSheet = false
                                onConnectBackup(provider)
                            },
                            onDismiss = { showProviderPickerSheet = false },
                        )
                    }

                    if (showDisconnectBackupDialog) {
                        DisconnectBackupDialog(
                            onDismiss = { showDisconnectBackupDialog = false },
                            onConfirm = {
                                showDisconnectBackupDialog = false
                                onDisconnectBackupTap()
                            },
                        )
                    }

                    if (showRestoreBackupDialog) {
                        RestoreBackupDialog(
                            onDismiss = { showRestoreBackupDialog = false },
                            onConfirm = {
                                showRestoreBackupDialog = false
                                onRestoreBackupTap()
                            },
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_group_app)) {
                    SettingRow(
                        icon = DwIcons.Ruler,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_unit_preference_title),
                        onClick = { showUnitSheet = true },
                    ) {
                        UnitPreferenceBadge(preference = state.unitPreference)
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Clock,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_time_format),
                        onClick = { showTimeFormatSheet = true },
                    ) {
                        TimeFormatBadge(preference = state.timeFormatPreference)
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Globe,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_language),
                        onClick = { showLanguageDialog = true },
                    ) {
                        LanguageBadge(tag = state.currentLocaleTag)
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_reminders)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingRow(
                            icon = DwIcons.Alarm,
                            iconBg = DwTintWater,
                            iconTint = DwWater,
                            label = stringResource(R.string.settings_row_reminder_type),
                            onClick = onExactAlarmTap,
                        ) {
                            AlarmTypeBadge(isExact = state.exactAlarmsEnabled)
                        }
                        HorizontalDivider(color = DwInk100)
                    }
                    SettingRow(
                        icon = DwIcons.Bell,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        label = stringResource(R.string.settings_row_notifications),
                    ) {
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = onNotificationsToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DwPaper,
                                checkedTrackColor = DwMagenta,
                                uncheckedThumbColor = DwPaper,
                                uncheckedTrackColor = DwInk200,
                                uncheckedBorderColor = DwInk200,
                            ),
                        )
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Glass,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_notification_amount),
                        onClick = { showNotificationAmountSheet = true },
                    ) {
                        RowValueTrailing(
                            value = state.notificationAmountMl.formatVolume(state.unitPreference)
                        )
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRowTimeSelector(
                        time = state.startTime?.let { TimeSelected(it.hour, it.minute) },
                        icon = DwIcons.Sun,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.label_start_time),
                        is24HourFormat = is24Hour,
                        message = stringResource(R.string.time_selector_message_start_time),
                        onStartTimeTap = onStartTimeChanged,
                    )
                    HorizontalDivider(color = DwInk100)
                    SettingRowTimeSelector(
                        time = state.endTime?.let { TimeSelected(it.hour, it.minute) },
                        icon = DwIcons.Moon,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        label = stringResource(R.string.label_end_time),
                        is24HourFormat = is24Hour,
                        message = stringResource(R.string.time_selector_message_end_time),
                        onStartTimeTap = onEndTimeChanged,
                    )
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Repeat,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_interval),
                        onClick = { showIntervalSheet = true },
                    ) {
                        RowValueTrailing(
                            value = stringResource(
                                R.string.settings_interval_value,
                                state.intervalMinutes
                            )
                        )
                    }
                    HorizontalDivider(color = DwInk100)
                    ActiveDaysSettingRow(
                        activeDays = state.activeDays,
                        onDayToggled = onActiveDayToggled,
                        onEnableAllDays = onEnableAllDays,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_overlays)) {
                    SettingRow(
                        icon = DwIcons.Sparkle,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        label = stringResource(R.string.settings_row_goal_celebrations),
                    ) {
                        Switch(
                            checked = state.goalReachedCelebrationsEnabled,
                            onCheckedChange = onGoalCelebrationsToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DwPaper,
                                checkedTrackColor = DwMagenta,
                                uncheckedThumbColor = DwPaper,
                                uncheckedTrackColor = DwInk200,
                                uncheckedBorderColor = DwInk200,
                            ),
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_privacy_legal)) {
                    SettingRow(
                        icon = DwIcons.ShieldCheck,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_share_usage_data),
                    ) {
                        Switch(
                            checked = state.isTrackingEnabled,
                            onCheckedChange = onTrackingToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DwPaper,
                                checkedTrackColor = DwMagenta,
                                uncheckedThumbColor = DwPaper,
                                uncheckedTrackColor = DwInk200,
                                uncheckedBorderColor = DwInk200,
                            ),
                        )
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Info,
                        iconBg = DwInk100,
                        iconTint = DwInk500,
                        label = stringResource(R.string.settings_row_privacy_policy_terms_of_use),
                        onClick = onPrivacyPolicyAndTermsOfUseTap,
                    ) {
                        RowChevronTrailing()
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    SettingRow(
                        icon = DwIcons.Share,
                        iconBg = DwInk100,
                        iconTint = DwInk500,
                        label = stringResource(R.string.settings_row_share),
                        onClick = onShareTap,
                    ) {
                        RowChevronTrailing()
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Star,
                        iconBg = DwInk100,
                        iconTint = DwInk500,
                        label = stringResource(R.string.settings_row_rate),
                        onClick = onRateTap,
                    ) {
                        RowChevronTrailing()
                    }
                    if (FeatureFlags.requestFeatureEnabled) {
                        HorizontalDivider(color = DwInk100)
                        SettingRow(
                            icon = DwIcons.Lightbulb,
                            iconBg = DwInk100,
                            iconTint = DwInk500,
                            label = stringResource(R.string.settings_row_request_feature),
                            onClick = onRequestFeatureTap,
                        ) {
                            RowChevronTrailing()
                        }
                    }
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Flag,
                        iconBg = DwInk100,
                        iconTint = DwInk500,
                        label = stringResource(R.string.settings_row_report_problem),
                        onClick = onReportProblemTap,
                    ) {
                        RowChevronTrailing()
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DwDanger.copy(alpha = 0.06f))
                        .clickable(onClick = onClearDataTap)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(DwDanger.copy(alpha = 0.12f)),
                    ) {
                        Icon(
                            imageVector = DwIcons.Trash,
                            contentDescription = null,
                            tint = DwDanger,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_row_reset_data),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DwDanger,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = DwIcons.ChevronRight,
                        contentDescription = null,
                        tint = DwDanger.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            item {
                BuildInfoFooter(
                    appVersion = state.appVersion,
                    buildVariant = state.buildVariant,
                )
            }
        }
    }

    // ── Bottom sheets ─────────────────────────────────────────────────────────
    if (showGoalSheet) {
        GoalBottomSheet(
            goalMl = state.dailyGoalMl,
            onGoalChanged = onGoalChanged,
            onDismiss = { showGoalSheet = false },
            unitPreference = state.unitPreference,
        )
    }

    if (showUnitsSheet) {
        UnitsBottomSheet(
            currentUnit = state.unit,
            onUnitChanged = onUnitChanged,
            onDismiss = { showUnitsSheet = false },
        )
    }

    if (showIntervalSheet) {
        IntervalBottomSheet(
            currentMinutes = state.intervalMinutes,
            windowMinutes = windowMinutes,
            onIntervalChanged = onIntervalChanged,
            onDismiss = { showIntervalSheet = false },
        )
    }

    if (showNotificationAmountSheet) {
        NotificationAmountBottomSheet(
            currentAmountMl = state.notificationAmountMl,
            onAmountChanged = onNotificationAmountChanged,
            onDismiss = { showNotificationAmountSheet = false },
            unitPreference = state.unitPreference,
        )
    }

    if (showTimeFormatSheet) {
        TimeFormatBottomSheet(
            current = state.timeFormatPreference,
            onPreferenceChanged = onTimeFormatChanged,
            onDismiss = { showTimeFormatSheet = false },
        )
    }

    if (showUnitSheet) {
        UnitPreferenceBottomSheet(
            current = state.unitPreference,
            onPreferenceChanged = onUnitPreferenceChanged,
            onDismiss = { showUnitSheet = false },
        )
    }

    // ── Language dialog ───────────────────────────────────────────────────────
    if (showLanguageDialog) {
        LanguageDialog(
            current = state.currentLocaleTag,
            onLanguageSelected = onLanguageChanged,
            onDismiss = { showLanguageDialog = false },
        )
    }
}

// ── Revoke tracking dialog ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RevokeTrackingDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_revoke_tracking_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.settings_revoke_tracking_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.settings_revoke_tracking_cta_confirm),
                onClick = onConfirm,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearAllDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var countdown by remember { mutableIntStateOf(5) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_row_reset_data),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DwDanger,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.settings_clear_data_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onConfirm,
                enabled = countdown == 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (countdown == 0) DwDanger else DwInk200,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DwDanger,
                    disabledContentColor = DwInk400,
                ),
            ) {
                Text(
                    text = if (countdown > 0) stringResource(
                        R.string.settings_clear_data_cta_countdown,
                        countdown
                    ) else stringResource(R.string.settings_clear_data_cta_confirm),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Permission denied dialog ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationPermissionDeniedDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
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
                text = stringResource(
                    R.string.notif_disabled_body,
                    stringResource(R.string.app_name)
                ),
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
                text = stringResource(R.string.action_not_now),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisableNotificationsDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_notif_disable_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.settings_notif_disable_body,
                    stringResource(R.string.app_name)
                ),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.action_continue_to_settings),
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = stringResource(R.string.action_not_now),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Draw-overlay instructions dialog ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawOverlayInstructionsDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    val context = LocalContext.current

    val title = if (!Settings.canDrawOverlays(context)) {
        stringResource(R.string.overlay_allow_over_other_apps)
    } else {
        stringResource(R.string.settings_overlay_disable_title)
    }

    val instructions = if (!Settings.canDrawOverlays(context)) {
        stringResource(R.string.settings_overlay_allow_instructions)
    } else {
        stringResource(R.string.settings_overlay_disable_instructions)
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = instructions,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.action_continue_to_settings),
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = stringResource(R.string.action_not_now),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Reusable setting row composables ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingRowTimeSelector(
    time: TimeSelected?,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier,
    is24HourFormat: Boolean = rememberTimePickerState().is24hour,
    message: String? = null,
    onStartTimeTap: (TimeSelected) -> Unit,
) {
    var showModal by rememberSaveable { mutableStateOf(false) }

    SettingRow(
        modifier = modifier,
        icon = icon,
        iconBg = iconBg,
        iconTint = iconTint,
        label = label,
        onClick = { showModal = !showModal },
    ) {
        RowValueTrailing(
            value = time?.let { formatTime(it.hour, it.minute, is24HourFormat) }.orEmpty(),
        )
    }

    if (showModal) {
        AppTimeSelectorDialog(
            time = time,
            is24HourFormat = is24HourFormat,
            onTimeSelected = onStartTimeTap,
            canChangeTimeFormat = false,
            message = message,
            onDismiss = { showModal = false },
        )
    }
}

@Composable
private fun ProfileCard(
    state: SettingsState,
    onUsernameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(gradientMagentaHero)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
        ) {
            state.username?.let {
                Text(
                    text = it.take(1).uppercase(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
            } ?: Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_drink_holo_dark),
                contentDescription = null,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.username ?: "Good morning",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${state.weightKg.toInt()} ${state.weightUnit.label} · daily goal ${
                    formatGoal(
                        state.dailyGoalMl
                    )
                }",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }

        EditButton(
            username = state.username,
            onUsernameChanged = onUsernameChanged,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = TextUnit(0.10f, TextUnitType.Em),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(modifier = Modifier.outlineBorderStyle()) {
            content()
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

/**
 * Reminders-group row exposing the active weekday toggles. Keeps the standard settings-row framing
 * (icon + label, with an "enable all" affordance) but hosts the [DaysOfWeekToggle] underneath so the
 * 7 day buttons can wrap on narrow screens instead of being clipped.
 */
@Suppress("MagicNumber")
@Composable
private fun ActiveDaysSettingRow(
    activeDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    onEnableAllDays: () -> Unit,
) {
    val allEnabled = activeDays.size == 7
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DwTintWater),
            ) {
                Icon(
                    imageVector = DwIcons.Alarm,
                    contentDescription = null,
                    tint = DwWater,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = stringResource(R.string.reminders_section_active_days),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(
                visible = !allEnabled,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = onEnableAllDays,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.SevenDaysCheck,
                        contentDescription = stringResource(R.string.reminders_enable_all_days),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        DaysOfWeekToggle(
            activeDays = activeDays,
            onDayToggled = onDayToggled,
            centered = false,
        )
    }
}

@Composable
private fun RowValueTrailing(value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = DwInk500,
        )
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun RowChevronTrailing() {
    Icon(
        imageVector = DwIcons.ChevronRight,
        contentDescription = null,
        tint = DwInk400,
        modifier = Modifier.size(16.dp),
    )
}

private fun formatGoal(ml: Float): String = "${ml.sanitizeZeroDecimals()} mL"

private fun formatTime(hour: Int, minute: Int, is24Hour: Boolean = false): String {
    val mm = minute.toString().padStart(2, '0')
    return if (is24Hour) {
        "${hour.toString().padStart(2, '0')}:$mm"
    } else {
        val ampm = if (hour < 12) "a.m." else "p.m."
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$displayHour:$mm $ampm"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExactAlarmInstructionsDialog(
    isGranted: Boolean,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    val title = if (!isGranted) {
        stringResource(R.string.settings_reminder_type_enable_title)
    } else {
        stringResource(R.string.settings_reminder_type_disable_title)
    }

    val instructions = if (!isGranted) {
        stringResource(
            R.string.settings_reminder_type_enable_instructions,
            stringResource(R.string.app_name)
        )
    } else {
        stringResource(
            R.string.settings_reminder_type_disable_instructions,
            stringResource(R.string.app_name)
        )
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = instructions,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.action_continue_to_settings),
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = stringResource(R.string.action_not_now),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Exact alarm status badge ───────────────────────────────────────────────────

@Composable
private fun AlarmTypeBadge(isExact: Boolean) {
    val text = stringResource(
        if (isExact) R.string.settings_row_reminder_status
        else R.string.settings_row_reminder_status_approximate
    )
    val textColor = if (isExact) DwWater else DwInk400
    val bgColor = if (isExact) DwTintWater else DwInk100

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = textColor,
            )
        }
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun UnitPreferenceBadge(preference: UnitPreference) {
    val text = when (preference) {
        UnitPreference.ML -> stringResource(R.string.settings_unit_ml)
        UnitPreference.AUTO_LITERS -> stringResource(R.string.settings_unit_auto_liters)
        UnitPreference.FL_OZ -> stringResource(R.string.settings_unit_fl_oz)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(DwInk100)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = DwInk400,
            )
        }
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun TimeFormatBadge(preference: TimeFormatPreference) {
    val text = when (preference) {
        TimeFormatPreference.SYSTEM -> stringResource(R.string.settings_time_format_auto)
        TimeFormatPreference.HOUR_12 -> "12h"
        TimeFormatPreference.HOUR_24 -> "24h"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(DwInk100)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = DwInk400,
            )
        }
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun LanguageBadge(tag: String) {
    val text = if (tag.isEmpty()) {
        stringResource(R.string.settings_language_system)
    } else {
        Locale.forLanguageTag(tag)
            .getDisplayLanguage(Locale.forLanguageTag(tag))
            .replaceFirstChar { it.uppercase() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(DwInk100)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = DwInk400,
            )
        }
        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Purchase success dialog ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseSuccessDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(gradientMagentaHero),
            ) {
                Icon(
                    imageVector = DwIcons.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.settings_purchase_success_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.settings_purchase_success_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            DwPrimaryButton(
                text = stringResource(R.string.settings_purchase_success_cta),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreSuccessDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DwWater, Color(0xFF0D6B9A))
                        )
                    ),
            ) {
                Icon(
                    imageVector = DwIcons.Drop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.settings_restore_success_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.settings_restore_success_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            DwPrimaryButton(
                text = stringResource(R.string.settings_restore_success_cta),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Disconnect backup dialog ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisconnectBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_disconnect_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.backup_disconnect_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.backup_disconnect_confirm),
                onClick = onConfirm,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_restore_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.backup_restore_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = stringResource(R.string.backup_restore_confirm),
                onClick = onConfirm,
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

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun SettingsScreenPreview() {
    DrinkWaterAppTheme {
        SettingsContent(
            state = settingsStateMock,
            onNotificationsToggled = {},
            onUsernameChanged = {},
            onGoalChanged = {},
            onUnitChanged = { _, _ -> },
            onIntervalChanged = {},
            onNotificationAmountChanged = {},
            onStartTimeChanged = {},
            onEndTimeChanged = {},
            onShareTap = {},
            onRateTap = {},
            onRequestFeatureTap = {},
            onReportProblemTap = {},
            onGoalCelebrationsToggled = {},
            onExactAlarmTap = {},
            onClearDataTap = {},
            onRemoveAdsTap = {},
            onTimeFormatChanged = {},
            onUnitPreferenceChanged = {},
            onTrackingToggled = {},
            onPrivacyPolicyAndTermsOfUseTap = {},
            onLanguageChanged = {},
        )
    }
}

private val settingsStateMock = SettingsState(
    username = null,
    notificationsEnabled = false,
    intervalMinutes = 5,
    startTime = null,
    endTime = null,
    goalReachedCelebrationsEnabled = false,
    appVersion = "1.1.4-dev",
    buildVariant = "debug",
)
