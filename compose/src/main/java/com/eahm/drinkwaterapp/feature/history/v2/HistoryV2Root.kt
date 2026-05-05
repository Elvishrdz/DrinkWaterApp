package com.eahm.drinkwaterapp.feature.history.v2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwDanger
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk700
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwSuccess
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

// ── Entry point ──────────────────────────────────────────────────────────────

@Composable
internal fun HistoryV2Root(
    viewModel: HistoryV2ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HistoryV2Content(
        state = state,
        onSelectDay = viewModel::onSelectDay,
        onPrevDay = viewModel::onPrevDay,
        onNextDay = viewModel::onNextDay,
        onDelete = viewModel::onDelete,
        onUpdateRecord = viewModel::onUpdateRecord,
    )
}

// ── Content dispatcher ────────────────────────────────────────────────────────

@Composable
internal fun HistoryV2Content(
    state: HistoryV2State,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onUpdateRecord: (HistoryEntity) -> Unit,
) {
    when (state) {
        HistoryV2State.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = DwWater)
        }

        is HistoryV2State.Data -> HistoryV2Screen(
            state = state,
            onSelectDay = onSelectDay,
            onPrevDay = onPrevDay,
            onNextDay = onNextDay,
            onDelete = onDelete,
            onUpdateRecord = onUpdateRecord,
        )
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
private fun HistoryV2Screen(
    state: HistoryV2State.Data,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onUpdateRecord: (HistoryEntity) -> Unit,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var editingRecord by remember { mutableStateOf<HistoryUI?>(null) }
    var editAmountText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DwInk50),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            HistoryHeader()

            // Day strip
            DayStripRow(
                days = state.days,
                selectedKey = state.selectedDayKey,
                canGoPrev = state.canGoPrev,
                canGoNext = state.canGoNext,
                onSelectDay = onSelectDay,
                onPrevDay = onPrevDay,
                onNextDay = onNextDay,
            )

            // Scrollable body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Summary card
                item {
                    SummaryCard(
                        dayLabel = state.selectedDayLabel,
                        totalMl = state.totalMl,
                        drinkCount = state.drinkCount,
                        percent = state.percent,
                    )
                }

                // Hour groups or empty state
                if (state.records.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(state.records, key = { it.hour }) { group ->
                        HourGroupCard(
                            group = group,
                            expandedId = expandedId,
                            onToggleExpand = { id ->
                                expandedId = if (expandedId == id) null else id
                            },
                            onEdit = { record ->
                                editingRecord = record
                                editAmountText = record.amount
                            },
                            onDelete = { record ->
                                onDelete(record.toEntity())
                                expandedId = null
                            },
                        )
                    }
                }
            }
        }
    }

    // Edit dialog
    editingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { editingRecord = null },
            containerColor = DwPaper,
            title = {
                Text(
                    text = "Edit drink",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = DwInk900,
                )
            },
            text = {
                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = it.filter(Char::isDigit) },
                    label = { Text("Amount") },
                    suffix = { Text("mL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editAmountText.toFloatOrNull()
                        if (amount != null && amount > 0f) {
                            onUpdateRecord(record.toEntity().copy(amount = amount))
                            editingRecord = null
                        }
                    },
                ) {
                    Text(
                        text = "Save",
                        color = DwMagenta,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) {
                    Text(
                        text = "Cancel",
                        color = DwInk500,
                        fontFamily = PlusJakartaSans,
                    )
                }
            },
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DwPaper)
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "History",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = DwInk900,
        )
        // Calendar button — placeholder for future date-picker
        IconButton(onClick = {}) {
            Icon(
                imageVector = DwIcons.History,
                contentDescription = "Pick a date",
                tint = DwMagenta,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ── Day strip ─────────────────────────────────────────────────────────────────

@Composable
private fun DayStripRow(
    days: List<DayItem>,
    selectedKey: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedKey) {
        val index = days.indexOfFirst { it.key == selectedKey }
        if (index >= 0) {
            listState.animateScrollToItem(index = maxOf(0, index - 2))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DwPaper)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPrevDay,
            enabled = canGoPrev,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Previous day",
                tint = if (canGoPrev) DwInk500 else DwInk200,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180f),
            )
        }

        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(days, key = { it.key }) { day ->
                DayChip(
                    day = day,
                    isSelected = day.key == selectedKey,
                    onClick = { if (day.isEnabled) onSelectDay(day.key) },
                )
            }
        }

        IconButton(
            onClick = onNextDay,
            enabled = canGoNext,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Next day",
                tint = if (canGoNext) DwInk500 else DwInk200,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DayChip(
    day: DayItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dimmed = !day.isEnabled
    val bgColor = if (isSelected) DwMagenta else Color.Transparent
    val primaryTextColor = when {
        isSelected -> DwPaper
        dimmed -> DwInk200
        else -> DwInk700
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = day.isEnabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = day.weekday,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = primaryTextColor.copy(alpha = if (isSelected) 0.75f else 1f),
        )
        Text(
            text = day.dayNum,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = primaryTextColor,
        )
        // Goal-met dot or empty space placeholder to keep chip height stable
        if (day.goalMet) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) DwPaper else DwSuccess),
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    dayLabel: String,
    totalMl: Int,
    drinkCount: Int,
    percent: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwPaper)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dayLabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk400,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = totalMl.toString(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = DwInk900,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "mL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = DwInk400,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                text = "$drinkCount drink${if (drinkCount == 1) "" else "s"} · $percent% of goal",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = DwInk400,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Mini progress ring
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(60.dp)) {
                val strokeW = 6.dp.toPx()
                val r = size.minDimension / 2f - strokeW / 2f
                val topLeft = Offset(center.x - r, center.y - r)
                val arcSize = Size(r * 2f, r * 2f)

                // Track ring
                drawArc(
                    color = DwWaterLight.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize,
                )

                // Progress arc
                val sweep = (percent.coerceIn(0, 100) / 100f) * 360f
                if (sweep > 0f) {
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(DwWaterLight, DwWaterDeep),
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
            }
            Text(
                text = "$percent%",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = DwWaterDeep,
            )
        }
    }
}

// ── Hour group ────────────────────────────────────────────────────────────────

@Composable
private fun HourGroupCard(
    group: HourGroup,
    expandedId: String?,
    onToggleExpand: (String) -> Unit,
    onEdit: (HistoryUI) -> Unit,
    onDelete: (HistoryUI) -> Unit,
) {
    val groupTotalMl = group.items.sumOf { it.amount.toIntOrNull() ?: 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwPaper),
    ) {
        // Group header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HourPill(hour = group.hour)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = partOfDay(group.hour),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk500,
            )
            Text(
                text = " · ",
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk300,
            )
            Text(
                text = "$groupTotalMl mL",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DwInk500,
            )
        }

        HorizontalDivider(color = DwInk100)

        group.items.forEachIndexed { index, item ->
            RecordRow(
                item = item,
                isExpanded = expandedId == item.id,
                onToggle = { onToggleExpand(item.id) },
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item) },
            )
            if (index < group.items.lastIndex) {
                HorizontalDivider(color = DwInk100)
            }
        }
    }
}

@Composable
private fun HourPill(hour: Int) {
    val (bgColor, fgColor) = when {
        hour in 5..7 -> DwTintMagenta to DwMagenta
        hour in 8..16 -> DwTintWater to DwWater
        hour in 17..19 -> DwTintMagenta to DwMagenta
        else -> DwInk100 to DwInk500
    }
    val icon = if (hour in 5..19) DwIcons.Sun else DwIcons.Moon

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fgColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = hourLabel(hour),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = fgColor,
        )
    }
}

// ── Record row ────────────────────────────────────────────────────────────────

@Composable
private fun RecordRow(
    item: HistoryUI,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Glass icon box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DwTintWater),
            ) {
                Icon(
                    imageVector = DwIcons.Glass,
                    contentDescription = null,
                    tint = DwWater,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount + timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.amount} mL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DwInk900,
                )
                Text(
                    text = formatTime12h(item.timestamp),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = DwInk400,
                )
            }

            // Chevron rotates when expanded
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = null,
                tint = DwInk300,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 90f else 0f),
            )
        }

        // Expanded edit / delete actions
        AnimatedVisibility(visible = isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DwWater),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DwWater),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.Ruler,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DwDanger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DwDanger),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.Trash,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(DwInk100),
        ) {
            Icon(
                imageVector = DwIcons.Glass,
                contentDescription = null,
                tint = DwInk300,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "No drinks logged",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = DwInk700,
        )
        Text(
            text = "Pick another day, or head to Today to add one.",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = DwInk400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun partOfDay(hour: Int): String = when {
    hour in 5..11 -> "Morning"
    hour in 12..16 -> "Afternoon"
    hour in 17..20 -> "Evening"
    else -> "Night"
}

private fun hourLabel(hour: Int): String {
    val period = if (hour < 12) "a.m." else "p.m."
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$h12 $period"
}

private fun formatTime12h(dt: LocalDateTime): String {
    val h = dt.hour
    val m = dt.minute
    val period = if (h < 12) "a.m." else "p.m."
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, period)
}
