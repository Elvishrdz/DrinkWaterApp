package com.eahm.drinkwaterapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import kotlinx.coroutines.launch

class MainViewModel(
    private val drinkAlarmManager: DrinkAlarmManager,
) : ViewModel() {

    fun onResume() {
        viewModelScope.launch {
            drinkAlarmManager.onResume()
        }
    }
}
