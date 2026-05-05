package com.eahm.drinkwaterapp

import androidx.lifecycle.ViewModel
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager

class MainViewModel(
    private val drinkAlarmManager: DrinkAlarmManager,
) : ViewModel() {

    fun onResume() {
        drinkAlarmManager.onResume()
    }
}
