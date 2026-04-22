package com.eahm.drinkwaterapp.feature.drink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class DrinkState(
    val records: List<HistoryEntity>,
)

internal class DrinkViewModel(
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    private val records = dao.getAll()

    val firstSetup = prefsStorage.getFirstSetupDone()

    val state = combine(
        records,
        firstSetup
    ) { records, _ ->
        DrinkState(
            records = records,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DrinkState(records = emptyList()),
    )

    // press the drink button to start
    // set an alarm in the given time
    // show notification
    fun setAlarm() {
        // when no alarm is set, update the interval immediately.
        // when alarm is set, schedule an update of the interval at the next iteration.
    }

    fun onDrink() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val record = HistoryEntity(
                    amount = 200f,
                    status = Status.IN_APP,
                )

                dao.insertAll(record)
            }
        }
    }

    fun onDelete(it: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(it)
            }
        }
    }
}