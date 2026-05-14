package com.eahm.drinkwaterapp.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.eahm.drinkwaterapp.R
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
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.sanitizeZeroDecimals
import com.eahm.drinkwaterapp.core.presentation.composable.BuildInfoFooter
import com.eahm.drinkwaterapp.feature.settings.ui.EditButton
import com.eahm.drinkwaterapp.feature.settings.ui.GoalBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.IntervalBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.NotificationAmountBottomSheet
import com.eahm.drinkwaterapp.feature.settings.ui.UnitsBottomSheet
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorDialog
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private val gradientMagentaHero = Brush.verticalGradient(
    colors = listOf(Color(0xFFB83A95), DwMagentaDark)
)

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }

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

    fun handleRequestFeature() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data =
                "mailto:drink@gmail.com?subject=New%20feature%20request%20for%20DrinkWater%20App&body=Hi%20developers%20at%20DrinkWaterApp%20I%20would%20like%20to%20request%20the%20following%20feature%20for%20a%20future%20app%20release%3A%0A%0A".toUri()
        }
        context.startActivity(intent)
    }

    fun handleReportProblem() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data =
                "mailto:drink@gmail.com?subject=Problem%20report%20for%20DrinkWater%20App&body=Hi%20developers%20at%20DrinkWaterApp%20I%20would%20like%20to%20report%20the%20following%20problem%3A%0A%0A".toUri()
        }
        context.startActivity(intent)
    }

    fun handleShare() {
        val shareIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(R.string.settings_share_text)
                )
                type = "text/plain"
            },
            null,
        )
        context.startActivity(shareIntent)
    }

    fun handleRate() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        context.startActivity(intent)
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
        onShareTap = ::handleShare,
        onRateTap = ::handleRate,
        onRequestFeatureTap = ::handleRequestFeature,
        onReportProblemTap = ::handleReportProblem,
        onGoalCelebrationsToggled = { handleDrawOverlayToggle() },
        onClearDataTap = { showClearDataDialog = true },
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
                viewModel.clearAllData()
            },
        )
    }

    // ── Draw-overlay instructions dialog ─────────────────────────────────────
    if (showOverlayInstructionsDialog) {
        DrawOverlayInstructionsDialog(
            onDismiss = { showOverlayInstructionsDialog = false },
            onGoToSettings = {
                showOverlayInstructionsDialog = false
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
                drawOverlayLauncher.launch(intent)
            },
        )
    }
}

// ── Content composable ────────────────────────────────────────────────────────

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
    onClearDataTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        RowValueTrailing(value = formatGoal(state.dailyGoalMl))
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

            item {
                SettingsSection(title = stringResource(R.string.settings_section_reminders)) {
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
                        icon = DwIcons.Clock,
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
                    SettingRowTimeSelector(
                        time = state.startTime?.let { TimeSelected(it.hour, it.minute) },
                        icon = DwIcons.Sun,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.label_start_time),
                        onStartTimeTap = onStartTimeChanged,
                    )
                    HorizontalDivider(color = DwInk100)
                    SettingRowTimeSelector(
                        time = state.endTime?.let { TimeSelected(it.hour, it.minute) },
                        icon = DwIcons.Moon,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        label = stringResource(R.string.label_end_time),
                        onStartTimeTap = onEndTimeChanged,
                    )
                    HorizontalDivider(color = DwInk100)
                    SettingRow(
                        icon = DwIcons.Glass,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        label = stringResource(R.string.settings_row_notification_amount),
                        onClick = { showNotificationAmountSheet = true },
                    ) {
                        RowValueTrailing(
                            value = stringResource(
                                R.string.settings_notification_amount_value,
                                state.notificationAmountMl.toInt()
                            )
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_overlays)) {
                    SettingRow(
                        icon = DwIcons.Bell,
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
        )
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
                text = stringResource(R.string.settings_notif_disable_body),
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
            value = time?.let { formatTime(it.hour, it.minute) }.orEmpty(),
        )
    }

    if (showModal) {
        AppTimeSelectorDialog(
            time = time,
            is24HourFormat = is24HourFormat,
            onTimeSelected = onStartTimeTap,
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

private fun formatTime(hour: Int, minute: Int): String {
    val ampm = if (hour < 12) "a.m." else "p.m."
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $ampm"
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
            onClearDataTap = {},
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
