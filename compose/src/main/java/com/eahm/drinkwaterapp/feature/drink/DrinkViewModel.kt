package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
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

internal class DrinkViewModel(
    val application: Application,
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    private val records = dao.getAll()

    private val firstSetup = prefsStorage.getFirstSetupDone()

    val state = combine(
        records,
        firstSetup,
        drinkAlarmManager.state,
    ) { records, _, drinkAlarmState ->
        DrinkState(
            records = records,
            isAlarmActive = drinkAlarmState.active,
            alarmInterval = drinkAlarmState.interval,
            alarmRemainingTime = drinkAlarmManager.remainingTime(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DrinkState(
            records = emptyList(),
            isAlarmActive = false,
            alarmInterval = null,
            alarmRemainingTime = "",
        ),
    )

    fun onDrink() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val record = HistoryEntity(
                    amount = 200f,
                    status = Status.IN_APP,
                )

                dao.insertAll(record)

                drinkAlarmManager.setupAlarm(application)
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

    fun onStopAlarm() = drinkAlarmManager.stop()
    fun getRemainingAlarmTime() = drinkAlarmManager.remainingTime()
}