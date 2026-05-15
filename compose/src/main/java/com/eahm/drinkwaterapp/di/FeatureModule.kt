package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.MainViewModel
import com.eahm.drinkwaterapp.feature.dev.DevViewModel
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.AlarmCalculatorViewModel
import com.eahm.drinkwaterapp.feature.dev.alarmintervals.AlarmIntervalsViewModel
import com.eahm.drinkwaterapp.feature.dev.alarms.AlarmsViewModel
import com.eahm.drinkwaterapp.feature.dev.celebrations.CelebrationsViewModel
import com.eahm.drinkwaterapp.feature.dev.exactalarm.ExactAlarmDevViewModel
import com.eahm.drinkwaterapp.feature.dev.dailygoal.DailyGoalDevViewModel
import com.eahm.drinkwaterapp.feature.dev.menu.DevMenuViewModel
import com.eahm.drinkwaterapp.feature.dev.notifications.NotificationsViewModel
import com.eahm.drinkwaterapp.feature.dev.onboarding.OnboardingDevViewModel
import com.eahm.drinkwaterapp.feature.dev.streak.StreakDevViewModel
import com.eahm.drinkwaterapp.feature.drink.DrinkViewModel
import com.eahm.drinkwaterapp.feature.drink.alarm.HomeAlarmViewModel
import com.eahm.drinkwaterapp.feature.history.HistoryViewModel
import com.eahm.drinkwaterapp.feature.history.v2.HistoryV2ViewModel
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingViewModel
import com.eahm.drinkwaterapp.feature.settings.SettingsViewModel
import com.eahm.drinkwaterapp.feature.stats.CalendarViewModel
import com.eahm.drinkwaterapp.feature.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val featureModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::OnboardingViewModel)

    viewModelOf(::DrinkViewModel)
    viewModelOf(::HomeAlarmViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::HistoryV2ViewModel)
    viewModelOf(::SettingsViewModel)

    // development modules
    viewModelOf(::DevViewModel)
    viewModelOf(::DevMenuViewModel)
    viewModelOf(::AlarmCalculatorViewModel)
    viewModelOf(::AlarmIntervalsViewModel)
    viewModelOf(::AlarmsViewModel)
    viewModelOf(::DailyGoalDevViewModel)
    viewModelOf(::StreakDevViewModel)
    viewModelOf(::NotificationsViewModel)
    viewModelOf(::OnboardingDevViewModel)
    viewModelOf(::CelebrationsViewModel)
    viewModelOf(::ExactAlarmDevViewModel)
}
