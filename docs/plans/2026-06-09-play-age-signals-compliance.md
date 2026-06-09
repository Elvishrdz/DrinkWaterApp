# Plan: Play Age Signals API Integration & US Age-Verification Compliance

**Date:** 2026-06-09
**Status:** Proposed
**Driver:** Google Play age-verification changes for US states (Texas **SB 2420** in effect;
Utah, Louisiana and others following) + Brazil. Ref:
[Google Play developer help 16569691](https://support.google.com/googleplay/android-developer/answer/16569691),
[Play Age Signals API docs](https://developer.android.com/google/play/age-signals).

---

## Goal

Make DrinkWaterApp compliant with app-store age-verification laws by:

1. Integrating the **Play Age Signals API** to obtain each user's age/verification status at
   runtime.
2. Storing a **minor flag** internally (the one allowed downstream use: age-appropriate experience).
3. Driving **age-appropriate ad serving** off that flag via AdMob's `RequestConfiguration`
   (TFUA — tag for under the age of consent), while preserving our existing
   purchased = no-ads behavior and our banner-placeholder UX.
4. Completing the **Play Console** non-code obligations (IAP SKU age ratings, significant-change
   declarations, revocation reports).

> **Non-mandate note:** Google does *not* require calling the API; compliance is the developer's
> responsibility, and apps are **not** disabled if a parent denies a change. We integrate the API
> because we serve ads and IAP, which are the touchpoints the laws actually concern.

---

## What the law / Google requires (summary)

| Requirement                                                 | Mechanism                     | Applies to us?                                         |
|-------------------------------------------------------------|-------------------------------|--------------------------------------------------------|
| Obtain user age/verification signals                        | Play Age Signals API (beta)   | ✅ (ads + IAP)                                          |
| Use signals **only** for age-appropriate content/experience | "Limited Use" terms           | ✅ — no ad targeting, profiling, analytics on this data |
| Age ratings on in-app purchase SKUs                         | Play Console / Publishing API | ✅ — RevenueCat "remove ads" SKU                        |
| Declare "significant changes" needing parental approval     | Play Console (no new release) | ⚠️ likely minimal for a water app — assess             |
| Receive parental-approval revocation reports                | Play Console reports          | ✅ monitor                                              |
| (Recommended) verify untampered caller                      | Play Integrity API            | optional hardening                                     |

### Age Signals API facts

- Artifact: `com.google.android.play:age-signals:0.0.3`. Min API 23 — **we are already at minSdk 23
  **.
- No runtime permission needed; requires app installed/updated by Google Play (`APP_NOT_OWNED`,
  `SDK_VERSION_OUTDATED` are non-retryable).
- Call:
  `AgeSignalsManagerFactory.create(context).checkAgeSignals(AgeSignalsRequest.builder().build())`
  → `AgeSignalsResult`:
    - `userStatus()`: `VERIFIED` / `DECLARED` / `SUPERVISED` / `SUPERVISED_APPROVAL_PENDING` /
      `SUPERVISED_APPROVAL_DENIED` / `UNKNOWN` / `null` (not in scope region or not shared).
    - `ageLower()` (0–18), `ageUpper()` (2–18), `mostRecentApprovalDate()`, `installId()`.
- Retryable error codes (-1…-8, -100) → exponential backoff; cache is Play-managed (updated 2–8
  weeks
  after a birthday), so caching our own resolved result is acceptable.

---

## Current state (verified)

| Concern                                | Status                   | Location                                                         |
|----------------------------------------|--------------------------|------------------------------------------------------------------|
| AdMob init                             | ✅ `MobileAds.initialize` | `ads/.../DrinkWaterAppAds.kt:20`                                 |
| Banner ad UI (with placeholder states) | ✅                        | `ads/.../banner/DrinkWaterBannerAd.kt`, `AdMobBanner.kt`         |
| Ads shown only when not purchased      | ✅                        | `compose/.../core/billing/` (`PurchaseManager`, `PurchaseState`) |
| RemoveAds IAP (RevenueCat)             | ✅                        | `compose/.../core/billing/RevenueCatPurchaseManager.kt`          |
| `RequestConfiguration` / TFUA / TFCD   | ❌ never set              | —                                                                |
| Any age signal source                  | ❌ does not exist         | —                                                                |
| House "remove ads" fallback ad         | ❌ does not exist         | —                                                                |

---

## Design

### 1. New `:shared`/`compose` age module — `core/age`

```
core/age/
  AgeSignalsDataSource.kt      // wraps AgeSignalsManager, returns Result<AgeSignals, AgeError>
  AgeSignals.kt                // domain model: userStatus, ageLower, ageUpper, isMinor, installId
  AgeSignalsRepository.kt      // resolves + caches to DataStore, exposes StateFlow<AgeSignals?>
  AgeError.kt                  // typed errors mapped from AgeSignalsErrorCode (retryable vs not)
  AgeModule.kt                 // Koin
```

- `AgeSignalsDataSource` adapts the Play Tasks `addOnSuccessListener`/`addOnFailureListener`
  callback
  into a suspend `Result<AgeSignals, AgeError>` (use `suspendCancellableCoroutine`).
- **`isMinor`** derived rule (single source of truth):
    - `SUPERVISED`, `SUPERVISED_APPROVAL_*` → minor.
    - `ageUpper != null && ageUpper < 18` → minor.
    - `VERIFIED`/`DECLARED` with `ageLower >= 18` → adult.
    - `UNKNOWN` / `null` → treat as **non-minor** (default) but flag `unverified = true` so we can
      be
      conservative on ads if desired. (Decision point — see Open questions.)
- Persist last resolved `AgeSignals` in DataStore (`AgePreferences`) so ads can apply the right
  config immediately on next launch before the async call returns. Refresh on each app start.
- Retry policy: retry retryable error codes with exponential backoff (max ~3); non-retryable →
  cache as "unverified", do not loop.

### 2. AdMob age-appropriate configuration

In `DrinkWaterAppAds` (or a new `AdsConfigurator`), before/around `MobileAds.initialize`, apply:

```kotlin
val config = RequestConfiguration.Builder()
    .setTagForUnderAgeOfConsent(
        if (isMinor) RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
        else RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED
    )
    .build()
MobileAds.setRequestConfiguration(config)
```

- **Confirms the user's note:** AdMob *does* have a minor config — `TAG_FOR_UNDER_AGE_OF_CONSENT`
  (TFUA). Setting it true makes Google serve only age-appropriate / non-personalized ads. We still
  serve ads (per the user's decision), but our internal `isMinor` flag is stored regardless.
- Since `MobileAds.initialize` runs once at startup but age resolves async, plan to **re-apply
  `setRequestConfiguration` once age is resolved** (it affects subsequent ad requests). Banner
  requests made after resolution pick up the TFUA flag.

### 3. Banner placeholder + house "remove ads" fallback

Per the user's decision:

- Always keep the **banner placeholder** slot (existing behavior in `DrinkWaterBannerAd`).
- When no fillable/age-appropriate ad is returned, render a **default house ad** promoting the
  "Remove ads" purchase (reuse the existing `RemoveAdsBottomSheet` entry point on tap).
- This house fallback is a UI composable in `:ads` (`RemoveAdsHouseAd.kt`) shown on the banner's
  failed/empty state (`DrinkWaterBannerAdStates`).
- If the app **was purchased** (`PurchaseState` = removed) → **no ads at all**, no placeholder,
  no house ad. (Unchanged precedence; purchase wins over everything.)

Precedence: `purchased → render nothing` › `ad available → render ad (TFUA-aware)` ›
`no ad → render remove-ads house ad in placeholder`.

### 4. Play Console / operational (non-code)

- Add **age ratings to the RevenueCat "remove ads" SKU** in Play Console (or Publishing API).
- Complete the **Age signals** Play Console section; optionally configure custom age ranges
  (defaults 0-12 / 13-15 / 16-17 / 18+ are fine — likely no change).
- Assess whether any app behavior is a **"significant change"** requiring parental approval. For a
  water-reminder app this is likely **none**, but document the assessment and the Console
  declaration path.
- Set up monitoring of **parental-approval revocation reports**.
- Confirm the **`.dev`/`.staging` flavors** behavior — Age Signals returns `APP_NOT_OWNED` for
  sideloaded/non-Play installs, so dev builds will get `null`/error; ensure graceful fallback +
  `FakeAgeSignalsManager` (from `com.google.android.play.agesignals.testing`) for tests.

---

## Implementation steps

1. Add dependency `com.google.android.play:age-signals:0.0.3` to the version catalog + `:ads`
   (or a new `:core:age`) module.
2. Build `core/age` (data source, repository, model, error mapping, DataStore cache, Koin module);
   register module in the Application `startKoin`.
3. Resolve age on app start (Application or first composable scope); persist + expose
   `StateFlow<AgeSignals?>`.
4. Wire `isMinor`/`unverified` into ad serving: `RequestConfiguration` TFUA in `DrinkWaterAppAds`,
   re-applied after resolution.
5. Build the **house "remove ads" fallback ad** for the banner empty state; keep placeholder;
   keep purchased = no-ads precedence.
6. Tests: `FakeAgeSignalsManager`-backed unit tests for repository (each `userStatus`, retry on
   retryable codes, non-retryable → unverified); ViewModel/ads-decision tests; Compose test that
   purchased hides everything and minor → house ad on empty fill.
7. Play Console: SKU age ratings, Age signals section, significant-change assessment doc,
   revocation report monitoring.
8. (Optional hardening) Pair with **Play Integrity API** to verify untampered caller.

---

## Open questions / decisions

1. **`UNKNOWN`/`null` users** — serve normal (personalized) ads, or be conservative and apply TFUA?
   Default in this plan: normal ads, store `unverified = true`. Confirm.
update: 

2. **Where the age module lives** — `:ads` vs a new `:core:age` (or `:shared` for KMP/iOS reuse).
   The API is Android-only (Play), so it stays Android-side; recommend `:compose` `core/age` or a
   small `:age` module.


3. **Refresh cadence** — resolve once per cold start (proposed) vs also on resume. Play caches
   server-side; per-launch is sufficient.


4. **iOS** — this is an Android/Play requirement only; Apple has its own (Declared Age Range API).
   Out of scope here; note for a future iOS plan.

