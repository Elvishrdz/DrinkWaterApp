package com.eahm.drinkwaterapp.feature.history

import android.text.format.DateFormat
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.ads.banner.InlineBannerAd
import com.eahm.drinkwaterapp.ads.banner.rememberHistoryScreenLogsBannerAdState
import com.eahm.drinkwaterapp.ads.banner.rememberInterstitialState
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.history.ui.RecordAmountBottomSheet
import com.eahm.drinkwaterapp.ui.historylist.DateTimePickerButton
import com.eahm.drinkwaterapp.ui.historylist.HistoryTimeline
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorDialog
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import kotlin.random.Random

private const val INTERSTITIAL_PROBABILITY = 0.5f

@Composable
internal fun HistoryScreen(
    state: HistoryState.Data,
    onDelete: (HistoryEntity) -> Unit,
    onUpdate: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onGoToToday: () -> Unit,
) {
    val context = LocalContext.current
    val is24Hour = when (state.timeFormatPreference) {
        TimeFormatPreference.SYSTEM -> DateFormat.is24HourFormat(context)
        TimeFormatPreference.HOUR_12 -> false
        TimeFormatPreference.HOUR_24 -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Content(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            date = state.date,
            historyRecords = state.historyRecords,
            goalMl = state.goalMl,
            consumedMl = state.consumedMl,
            isToday = state.isToday,
            is24Hour = is24Hour,
            isAdFree = state.isAdFree,
            unitPreference = state.unitPreference,
            onDelete = onDelete,
            onUpdate = onUpdate,
            onDateChanged = onDateChanged,
            onGoToToday = onGoToToday,
        )
    }
}

@Composable
private fun Content(
    date: LocalDateTime,
    historyRecords: ImmutableList<HourGroup>,
    goalMl: Float,
    consumedMl: Float,
    isToday: Boolean,
    is24Hour: Boolean,
    isAdFree: Boolean,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
    onDelete: (HistoryEntity) -> Unit,
    onUpdate: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onGoToToday: () -> Unit,
) {
    val trackingService = koinInject<TrackingService>()

    // Hoisted here — survives date changes, item deletions, and group structural shifts.
    // The AdView is created and loadAd fired once per screen visit regardless of how many
    // times the list below recomposes or the banner moves between inside-group / between-group.
    val bannerAdState = rememberHistoryScreenLogsBannerAdState(enabled = !isAdFree)

    // Inline-edit state
    var editAmountTarget by remember { mutableStateOf<HistoryUI?>(null) }
    var editTimeTarget by remember { mutableStateOf<HistoryUI?>(null) }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    // The two most-recent records of the current day are exempt from the ad gate.
    val exemptIds = remember(historyRecords, isToday) {
        if (isToday) historyRecords.flatMap { it.items }.take(2).mapTo(mutableSetOf()) { it.id }
        else emptySet()
    }

    val activity = LocalActivity.current
    val interstitialState = rememberInterstitialState(enabled = !isAdFree)
    var interstitialReady by remember { mutableStateOf(false) }

    if (!isAdFree && interstitialState != null) {
        LaunchedEffect(Unit, exemptIds) {
            activity?.let {
                interstitialState.load(
                    activity = it,
                    onLoaded = { interstitialReady = true },
                    onFailedToLoad = { interstitialReady = false },
                    onDismissed = {
                        interstitialReady = false
                        pendingAction?.invoke()
                        pendingAction = null
                    },
                )
            }
        }
    }

    fun onRequestEdit(item: HistoryUI, action: () -> Unit) {
        // check if ad can be displayed
        val isExempt = item.id in exemptIds
        val adReady = !isAdFree && !isExempt && interstitialReady
        val showAd = adReady && Random.nextFloat() < INTERSTITIAL_PROBABILITY

        // or perform action
        if (showAd && interstitialState != null && activity != null) {
            pendingAction = action
            interstitialState.show(activity)
        } else {
            action()
        }
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.history_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionLabel(stringResource(R.string.history_section_select_date))
                    if (!isToday) {
                        TextButton(
                            onClick = onGoToToday,
                        ) {
                            Text(
                                text = stringResource(R.string.label_today),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                DateTimePickerButton(
                    selectedDateTime = date,
                    onDateTimeSelected = { dt ->
                        trackingService.trackHistoryFilterChanged()
                        onDateChanged(dt)
                    },
                    label = stringResource(R.string.history_select_date_label),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.history_section_summary))
                DaySummaryRow(
                    consumedMl = consumedMl,
                    goalMl = goalMl,
                    unitPreference = unitPreference
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel(
            text = stringResource(R.string.history_section_logs),
            modifier = Modifier.padding(horizontal = 22.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (historyRecords.isNotEmpty()) {
            HistoryTimeline(
                historyRecords = historyRecords,
                is24Hour = is24Hour,
                unitPreference = unitPreference,
                onDelete = {
                    trackingService.trackHistoryDeleteTapped()
                    onDelete(it.toEntity())
                },
                onEditAmount = { item ->
                    trackingService.trackHistoryEditAmountTapped()
                    onRequestEdit(item) { editAmountTarget = item }
                },
                onEditTime = { item ->
                    trackingService.trackHistoryEditTimeTapped()
                    onRequestEdit(item) { editTimeTarget = item }
                },
                secondItemContent = if (!isAdFree) {
                    { InlineBannerAd(state = bannerAdState) }
                } else null,
            )
        } else {
            val text = if (isToday) {
                stringResource(R.string.drink_log_empty_state)
            } else {
                stringResource(R.string.history_empty_title)
            }

            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            if (!isAdFree) {
                InlineBannerAd(
                    state = bannerAdState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                )
            }
        }
    }

    // Edit amount bottom sheet
    editAmountTarget?.let { target ->
        RecordAmountBottomSheet(
            currentAmountMl = target.amount.toFloat(),
            unitPreference = unitPreference,
            onAmountChanged = { newMl ->
                onUpdate(target.toEntity().copy(amount = newMl))
                editAmountTarget = null
            },
            onDismiss = { editAmountTarget = null },
        )
    }

    // Edit time dialog
    editTimeTarget?.let { target ->
        AppTimeSelectorDialog(
            time = TimeSelected(target.timestamp.hour, target.timestamp.minute),
            is24HourFormat = is24Hour,
            canChangeTimeFormat = false,
            message = stringResource(R.string.history_edit_time_message),
            onTimeSelected = { ts ->
                val newTs = LocalDateTime(
                    target.timestamp.year,
                    target.timestamp.monthNumber,
                    target.timestamp.dayOfMonth,
                    ts.hour,
                    ts.minute,
                    0,
                )
                onUpdate(target.toEntity().copy(timestamp = newTs))
                editTimeTarget = null
            },
            onDismiss = { editTimeTarget = null },
        )
    }
}

@Composable
private fun DaySummaryRow(
    consumedMl: Float,
    goalMl: Float,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatChip(
            label = stringResource(R.string.history_stat_consumed),
            value = consumedMl.formatVolume(unitPreference),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = stringResource(R.string.history_stat_goal),
            value = goalMl.formatVolume(unitPreference),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = DwInk300,
            letterSpacing = TextUnit(0.08f, TextUnitType.Em),
        )
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = DwInk500,
        letterSpacing = TextUnit(0.10f, TextUnitType.Em),
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun HistoryScreenPreview() {
    DrinkWaterAppTheme {
        Content(
            date = Mock.localDateTimeMock,
            historyRecords = Mock.hourGroupMock.toImmutableList(),
            goalMl = 2000f,
            consumedMl = 750f,
            isToday = false,
            is24Hour = false,
            isAdFree = false,
            onDelete = {},
            onUpdate = {},
            onDateChanged = {},
            onGoToToday = {},
        )
    }
}
