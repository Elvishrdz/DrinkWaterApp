package com.eahm.drinkwaterapp.feature.dev.historyinject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwDanger
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.historylist.DateTimePickerButton
import com.eahm.drinkwaterapp.ui.timepicker.AppTimePickerButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryInjectDevScreen(
    state: HistoryInjectDevState.Data,
    onAddEntry: () -> Unit,
    onRemoveEntry: (String) -> Unit,
    onUpdateEntry: (String, InjectEntry) -> Unit,
    onInsertAll: () -> Unit,
    onClearAll: () -> Unit,
    onSetClearByDay: (LocalDate) -> Unit,
    onClearByDay: () -> Unit,
) {
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Inject Records",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    IconButton(onClick = onAddEntry) {
                        Icon(
                            imageVector = DwIcons.Plus,
                            contentDescription = "Add entry",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // ── Entry rows ────────────────────────────────────────────────────
            items(state.entries, key = { it.id }) { entry ->
                InjectEntryRow(
                    entry = entry,
                    onUpdate = { updated -> onUpdateEntry(entry.id, updated) },
                    onRemove = { onRemoveEntry(entry.id) },
                )
            }

            // ── Insert All button ─────────────────────────────────────────────
            item {
                DwPrimaryButton(
                    text = "Insert All",
                    onClick = onInsertAll,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.insertResult.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.insertResult,
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = DwInk500,
                    )
                }
            }

            // ── Danger Zone ───────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DwInk100)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "DANGER ZONE",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DwDanger,
                    letterSpacing = TextUnit(0.10f, TextUnitType.Em),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showClearAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.5.dp, DwDanger),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DwDanger,
                    ),
                ) {
                    Text(
                        text = "Clear All Records",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── Clear by day ──────────────────────────────────────────────────
            item {
                Text(
                    text = "Clear by day:",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
                DateTimePickerButton(
                    selectedDateTime = state.clearByDay?.let { LocalDateTime(it, LocalTime(0, 0)) },
                    onDateTimeSelected = { onSetClearByDay(it.date) },
                    label = state.clearByDay?.toString() ?: "Select day",
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onClearByDay,
                    enabled = state.clearByDay != null,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(
                        1.5.dp,
                        if (state.clearByDay != null) DwDanger else DwInk400,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DwDanger,
                        disabledContentColor = DwInk400,
                    ),
                ) {
                    Text(
                        text = "Clear Selected Day",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showClearAllDialog) {
        ClearAllDialog(
            onDismiss = { showClearAllDialog = false },
            onConfirm = {
                showClearAllDialog = false
                onClearAll()
            },
        )
    }
}

@Composable
private fun InjectEntryRow(
    entry: InjectEntry,
    onUpdate: (InjectEntry) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Date picker
            DateTimePickerButton(
                selectedDateTime = entry.date?.let { LocalDateTime(it, LocalTime(entry.hour, entry.minute)) },
                onDateTimeSelected = { result -> onUpdate(entry.copy(date = result.date)) },
                label = entry.date?.toString() ?: "Pick date",
                modifier = Modifier.weight(1f),
            )

            // Time picker
            AppTimePickerButton(
                text = "%02d:%02d".format(entry.hour, entry.minute),
                time = TimeSelected(entry.hour, entry.minute),
                onTimeSelected = { t -> onUpdate(entry.copy(hour = t.hour, minute = t.minute)) },
            )

            // Delete button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DwDanger.copy(alpha = 0.08f)),
            ) {
                Icon(
                    imageVector = DwIcons.Trash,
                    contentDescription = "Remove",
                    tint = DwDanger,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        OutlinedTextField(
            value = entry.amountText,
            onValueChange = { onUpdate(entry.copy(amountText = it)) },
            label = { Text("Amount (mL)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearAllDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            Text(
                text = "Clear All Records?",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DwDanger,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "This will permanently delete every drink record from the database. This action cannot be undone.",
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                color = DwInk500,
            )
            Spacer(Modifier.height(24.dp))
            DwPrimaryButton(
                text = "Yes, Clear All",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            DwSecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
