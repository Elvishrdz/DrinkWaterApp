package com.eahm.drinkwaterapp.ads.banner

import androidx.compose.runtime.Composable
import com.eahm.drinkwaterapp.ads.BuildConfig

@Composable
fun rememberBelowMainScreenBannerAdState(enabled: Boolean = true): InlineBannerAdState {
    return rememberInlineBannerAdState(
        adUnitId = BuildConfig.BANNER_BELOW_MAIN_SCREEN,
        enabled = enabled,
    )
}

@Composable
fun rememberHistoryScreenLogsBannerAdState(enabled: Boolean = true): InlineBannerAdState {
    return rememberInlineBannerAdState(
        adUnitId = BuildConfig.BANNER_HISTORY_SCREEN_LOGS,
        enabled = enabled,
    )
}

@Composable
fun rememberStatsScreenComponentBannerAdState(enabled: Boolean = true): InlineBannerAdState {
    return rememberInlineBannerAdState(
        adUnitId = BuildConfig.BANNER_STATS_SCREEN_COMPONENT,
        enabled = enabled,
    )
}
