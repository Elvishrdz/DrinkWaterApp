package com.eahm.drinkwaterapp.feature.dev.historyinject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class HistoryInjectDevViewModel(
    database: AppDatabase,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    private val _state = MutableStateFlow(HistoryInjectDevState.Data())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    fun addEntry() {
        _state.update { current ->
            if (current !is HistoryInjectDevState.Data) return
            current.copy(entries = current.entries + InjectEntry())
        }
    }

    fun removeEntry(id: String) {
        _state.update { current ->
            if (current !is HistoryInjectDevState.Data) return
            val updated = current.entries.filter { it.id != id }
            current.copy(entries = updated.ifEmpty { listOf(InjectEntry()) })
        }
    }

    fun updateEntry(id: String, entry: InjectEntry) {
        _state.update { current ->
            if (current !is HistoryInjectDevState.Data) return
            current.copy(entries = current.entries.map { if (it.id == id) entry else it })
        }
    }

    fun insertAll() {
        val current = _state.value as? HistoryInjectDevState.Data ?: return
        viewModelScope.launch {
            var inserted = 0
            var skipped = 0
            withContext(Dispatchers.IO) {
                current.entries.forEach { entry ->
                    val amountMl = entry.amountText.trim().toFloatOrNull()
                    if (amountMl == null || amountMl <= 0f) { skipped++; return@forEach }
                    val date = entry.date ?: run { skipped++; return@forEach }
                    val timestamp = LocalDateTime(date, LocalTime(entry.hour, entry.minute))
                    dao.insertAll(
                        HistoryEntity(
                            amount = amountMl,
                            status = Status.IN_APP,
                            timestamp = timestamp,
                        )
                    )
                    inserted++
                }
            }
            _state.update { s ->
                (s as? HistoryInjectDevState.Data)?.copy(
                    insertResult = "Inserted: $inserted, Skipped: $skipped"
                ) ?: s
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAll()
            _state.update { s ->
                (s as? HistoryInjectDevState.Data)?.copy(insertResult = "All records cleared") ?: s
            }
        }
    }

    fun setClearByDay(date: LocalDate) {
        _state.update { s ->
            (s as? HistoryInjectDevState.Data)?.copy(clearByDay = date) ?: s
        }
    }

    fun clearByDay() {
        val current = _state.value as? HistoryInjectDevState.Data ?: return
        val date = current.clearByDay ?: return
        viewModelScope.launch {
            dao.deleteAllBetween(
                start = LocalDateTime(date, LocalTime(0, 0, 0)),
                end = LocalDateTime(date, LocalTime(23, 59, 59)),
            )
            _state.update { s ->
                (s as? HistoryInjectDevState.Data)?.copy(
                    insertResult = "Cleared records for $date"
                ) ?: s
            }
        }
    }
}
