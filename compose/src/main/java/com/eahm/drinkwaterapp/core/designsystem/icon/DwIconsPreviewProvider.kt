package com.eahm.drinkwaterapp.core.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class DwIconSample(val name: String, val vector: ImageVector)

class DwIconsPreviewProvider : PreviewParameterProvider<DwIconSample> {
    override val values = sequenceOf(
        DwIconSample("Drop", DwIcons.Drop),
        DwIconSample("DropFilled", DwIcons.DropFilled),
        DwIconSample("Wave", DwIcons.Wave),
        DwIconSample("WaveFilled", DwIcons.WaveFilled),
        DwIconSample("Settings", DwIcons.Settings),
        DwIconSample("SettingsFilled", DwIcons.SettingsFilled),
        DwIconSample("Glass", DwIcons.Glass),
        DwIconSample("Star", DwIcons.Star),
        DwIconSample("Plus", DwIcons.Plus),
        DwIconSample("ChevronRight", DwIcons.ChevronRight),
        DwIconSample("Bell", DwIcons.Bell),
        DwIconSample("Clock", DwIcons.Clock),
        DwIconSample("Sun", DwIcons.Sun),
        DwIconSample("Moon", DwIcons.Moon),
        DwIconSample("Share", DwIcons.Share),
        DwIconSample("History", DwIcons.History),
        DwIconSample("HistoryFilled", DwIcons.HistoryFilled),
        DwIconSample("Trash", DwIcons.Trash),
        DwIconSample("Flag", DwIcons.Flag),
        DwIconSample("Lightbulb", DwIcons.Lightbulb),
        DwIconSample("Ruler", DwIcons.Ruler),
        DwIconSample("Globe", DwIcons.Globe),
        DwIconSample("ShieldCheck", DwIcons.ShieldCheck),
        DwIconSample("IdCard", DwIcons.IdCard),
        DwIconSample("PulseHeart", DwIcons.PulseHeart),
        DwIconSample("Info", DwIcons.Info),
        DwIconSample("Close", DwIcons.Close),
        DwIconSample("Alarm", DwIcons.Alarm),
        DwIconSample("Repeat", DwIcons.Repeat),
        DwIconSample("Sparkle", DwIcons.Sparkle),
        DwIconSample("BarChart", DwIcons.BarChart),
        DwIconSample("BarChartFilled", DwIcons.BarChartFilled),
    )
}
