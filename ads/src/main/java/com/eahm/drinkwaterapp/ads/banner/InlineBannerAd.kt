package com.eahm.drinkwaterapp.ads.banner

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
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
 * Holds the [AdView] and its loaded state so they can be hoisted to a level that survives
 * structural list changes (date changes, item deletions, group splits).
 *
 * Create via [rememberInlineBannerAdState] at the screen/content composable level and pass the
 * resulting instance down to [InlineBannerAd].  This ensures the AdView is created and the ad
 * request is fired exactly once per screen visit, regardless of how many times the list below
 * recomposes or changes structure.
 */
@Stable
class InlineBannerAdState internal constructor(
    internal val adView: AdView,
    internal val isAdLoaded: MutableState<Boolean> = mutableStateOf(false),
    internal val isAdFailed: MutableState<Boolean> = mutableStateOf(false),
)

/**
 * Creates and remembers an [InlineBannerAdState].
 *
 * Must be called at a stable level in the composition (e.g. the screen's Content composable),
 * NOT inside a list loop or a slot lambda — that would defeat the purpose of hoisting.
 *
 * @param enabled When false the AdView is created but [AdView.loadAd] is never called.
 *   Pass `!isAdFree` here so that ad-free users do not trigger any network requests.
 */
@Composable
internal fun rememberInlineBannerAdState(
    adUnitId: String,
    enabled: Boolean = true
): InlineBannerAdState {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current

    val state = remember(enabled) {
        InlineBannerAdState(
            adView = AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, 360))
            }
        )
    }

    // Key is state.adView (stable via remember): runs exactly once on first composition,
    // disposes when the calling composable leaves the composition tree (screen nav-away).
    // enabled is captured from the first composition — sufficient because isAdFree almost
    // never flips during a single screen visit.
    DisposableEffect(state.adView) {
        if (enabled && !isInPreview) {
            state.adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    state.isAdLoaded.value = true
                    state.isAdFailed.value = false
                    Log.d(INLINE_TAG, "Inline banner ad loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    state.isAdFailed.value = true
                    Log.e(INLINE_TAG, "Inline banner ad failed to load: ${error.message}")
                }

                override fun onAdImpression() {
                    Log.d(INLINE_TAG, "Inline banner ad impression.")
                }

                override fun onAdClicked() {
                    Log.d(INLINE_TAG, "Inline banner ad clicked.")
                }
            }
            state.adView.loadAd(AdRequest.Builder().build())
        }

        onDispose {
            state.adView.destroy()
        }
    }

    return state
}

/**
 * Renders the inline banner ad described by [state].
 *
 * This composable is intentionally thin — it only displays the already-loaded [AdView] held by
 * [state].  All creation, loading, and lifecycle logic lives in [rememberInlineBannerAdState],
 * which must be called at a higher, structurally stable level so the AdView survives list
 * recompositions without being destroyed and re-requested.
 */
@Composable
fun InlineBannerAd(
    state: InlineBannerAdState,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit = {},
) {
    when {
        // Show the loaded ad (or immediately in the IDE for tooling feedback).
        state.isAdLoaded.value || LocalInspectionMode.current -> {
            Box(modifier = modifier.fillMaxWidth()) {
                BannerAd(state.adView)
            }
        }
        // No fillable ad: surface the house "remove ads" promo in the same slot.
        state.isAdFailed.value -> fallback()
    }
}

@Preview(apiLevel = 33)
@Composable
private fun InlineBannerAdPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        val state = rememberInlineBannerAdState(
            adUnitId = BuildConfig.BANNER_BELOW_MAIN_SCREEN,
        )
        InlineBannerAd(state = state)
    }
}
