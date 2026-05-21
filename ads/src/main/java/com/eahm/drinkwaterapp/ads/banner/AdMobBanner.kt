package com.eahm.drinkwaterapp.ads.banner

import android.app.Activity
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.eahm.drinkwaterapp.ads.BuildConfig
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@Composable
fun AdMobBanner(
    @StringRes unitIdRes: Int,
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.FULL_BANNER
) {
    val unitId = stringResource(unitIdRes)
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(10)),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun AdMobAdaptiveBanner(
    modifier: Modifier = Modifier,
    margin: Dp = 0.dp,
    strokeColor: Color,
    strokeWidth: Dp = 0.2.dp,
    @StringRes unitIdRes: Int
) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerSize.width
    val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
        context,
        screenWidth
    )
    val unitId = stringResource(unitIdRes)
    AndroidView(
        modifier = modifier
            .padding(margin)
            .border(strokeWidth, strokeColor, RoundedCornerShape(10))
            .clip(RoundedCornerShape(10)),
        factory = {
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

enum class CollapseDirection(val value: String) {
    TOP("top"),
    BOTTOM("bottom")
}

@Composable
fun AdMobCollapsableBanner(
    modifier: Modifier = Modifier,
    @StringRes unitIdRes: Int,
    adSize: AdSize = AdSize.FULL_BANNER,
    collapseDirection: CollapseDirection = CollapseDirection.TOP
) {
    val unitId = stringResource(unitIdRes)
    AndroidView(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(10)),
        factory = { context ->
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = unitId
                val extras = Bundle()
                extras.putString("collapsible", collapseDirection.value)
                loadAd(
                    AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                        .build()
                )
            }
        }
    )
}


fun Activity.loadHistoryScreenEditLogFullScreenAd(
    adRequest: AdRequest = AdRequest.Builder().build(),
    onAdLoaded: (InterstitialAd) -> Unit,
    onAdFailedToLoad: (LoadAdError) -> Unit,
    onAdDismissed: () -> Unit
) {
    loadFullScreenAd(
        unitIdRes = BuildConfig.INTERSTITIAL_HISTORY_SCREEN_EDIT,
        adRequest = adRequest,
        onAdLoaded = onAdLoaded,
        onAdFailedToLoad = onAdFailedToLoad,
        onAdDismissed = onAdDismissed,
    )
}

private fun Activity.loadFullScreenAd(
    unitIdRes: String,
    adRequest: AdRequest = AdRequest.Builder().build(),
    onAdLoaded: (InterstitialAd) -> Unit,
    onAdFailedToLoad: (LoadAdError) -> Unit,
    onAdDismissed: () -> Unit
) {
    InterstitialAd.load(
        this,
        unitIdRes,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                onAdLoaded(interstitialAd)
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdDismissed()
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                onAdFailedToLoad(loadAdError)
            }
        }
    )
}

class InlineInterstitialAdState internal constructor(
    val unitIdRes: String,
) {
    private val interstitial: MutableState<InterstitialAd?> = mutableStateOf(null)
    var isLoading: Boolean = false

    fun load(
        activity: Activity,
        onLoaded: () -> Unit,
        onFailedToLoad: () -> Unit,
        onDismissed: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true
        activity.loadFullScreenAd(
            unitIdRes = unitIdRes,
            onAdLoaded = { ad ->
                interstitial.value = ad
                onLoaded()
                isLoading = false
            },
            onAdFailedToLoad = {
                onFailedToLoad()
                isLoading = false
            },
            onAdDismissed = {
                interstitial.value = null
                onDismissed()
                isLoading = false
            },
        )
    }


    fun show(
        activity: Activity,
    ) {
        interstitial.value?.show(activity)
    }
}

@Composable
fun rememberInterstitialState(enabled: Boolean = true): InlineInterstitialAdState? {
    return remember(enabled) {
        if (enabled) {
            InlineInterstitialAdState(
                unitIdRes = BuildConfig.INTERSTITIAL_HISTORY_SCREEN_EDIT,
            )
        } else null
    }
}
