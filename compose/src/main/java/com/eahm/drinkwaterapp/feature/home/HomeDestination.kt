package com.eahm.drinkwaterapp.feature.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons

enum class HomeDestination(
    @param:StringRes val labelResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    TODAY(R.string.label_today, DwIcons.Drop, DwIcons.DropFilled),
    HISTORY(R.string.history_title, DwIcons.History, DwIcons.HistoryFilled),
    STATS(R.string.stats_title, DwIcons.BarChart, DwIcons.BarChartFilled),
    SETTINGS(R.string.settings_title, DwIcons.Settings, DwIcons.SettingsFilled),
    DEV(R.string.dev_title, DwIcons.Ruler, DwIcons.Ruler),
}
