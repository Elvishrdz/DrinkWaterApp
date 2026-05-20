package com.eahm.drinkwaterapp.ads.banner

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.ads.BuildConfig
import com.eahm.drinkwaterapp.ads.util.BannerAd
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val INLINE_TAG = "DrinkWaterInlineAd"

/**
 * An inline banner ad for use inside scrollable lists or content areas.
 *
 * Improvements over [DrinkWaterBannerAd]:
 * - Loads the ad exactly once (the [DisposableEffect] key is the stable remembered [AdView]),
 *   so navigating away and back, or any recomposition, does not fire a new ad request.
 * - The ad view is only rendered after [AdListener.onAdLoaded] fires, preventing a blank
 *   placeholder from showing before the creative arrives.
 * - [AdView.destroy] is called when the composable leaves the composition, releasing resources.
 */
@Composable
fun InlineBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current

    // Tracks whether the ad has finished loading.
    val isAdLoaded = remember { mutableStateOf(false) }

    // The AdView instance is created once and survives recompositions.
    val adView = remember {
        AdView(context).apply {
            adUnitId = BuildConfig.ADS_BANNER_UNIT_ID
            setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, 360))
        }
    }

    // adView is stable (created via remember), so this DisposableEffect runs exactly once:
    // on first composition it sets up the listener and fires the ad request;
    // on disposal it cleans up to avoid memory leaks.
    DisposableEffect(adView) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isAdLoaded.value = true
                Log.d(INLINE_TAG, "Inline banner ad loaded.")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(INLINE_TAG, "Inline banner ad failed to load: ${error.message}")
            }

            override fun onAdImpression() {
                Log.d(INLINE_TAG, "Inline banner ad impression.")
            }

            override fun onAdClicked() {
                Log.d(INLINE_TAG, "Inline banner ad clicked.")
            }
        }

        if (!isInPreview) {
            adView.loadAd(AdRequest.Builder().build())
        }

        onDispose {
            adView.destroy()
        }
    }

    // Render the ad only once it has loaded (or immediately in preview for tooling feedback).
    if (isAdLoaded.value || isInPreview) {
        Box(modifier = modifier.fillMaxWidth()) {
            BannerAd(adView)
        }
    }
}

@Preview(apiLevel = 33)
@Composable
private fun InlineBannerAdPreview() {
    Surface(color = MaterialTheme.colorScheme.background) { InlineBannerAd() }
}
