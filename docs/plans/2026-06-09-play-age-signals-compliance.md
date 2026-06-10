# Plan: Play Age Signals API Integration & US Age-Verification Compliance

**Date:** 2026-06-09
**Status:** Implemented (code steps 1–10) · Play Console steps (11) pending · Play Integrity (12) optional

> **Implementation note (2026-06-10):** All code stages are done and committed (one commit per
> stage), debug app assembles, 26 `:age` unit tests pass. Remaining work is **non-code**:
> step 11 (Play Console — IAP SKU age ratings, Age signals section, significant-change assessment,
> revocation-report monitoring) must be done in the Play Console; step 12 (Play Integrity) is
> optional hardening. The dev menu (`Dev → Age Signals`) drives the real pipeline for testing.
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
3. Providing a **one-time bootstrap age-collection screen**, shown only on first run when the API
   gives no usable signal (`UNKNOWN` or error) and we have no stored age — so we always hold an
   initial age regardless of API availability. A definitive API response (real status or
   "not covered") skips it; once any age is stored it is never shown again.
4. Driving **age-appropriate ad serving** off that flag via AdMob's `RequestConfiguration`
   (TFUA — tag for under the age of consent), while preserving our existing
   purchased = no-ads behavior and our banner-placeholder UX.
5. Completing the **Play Console** non-code obligations (IAP SKU age ratings, significant-change
   declarations, revocation reports).

> **Non-mandate note:** Google does *not* require calling the API; compliance is the developer's
> responsibility, and apps are **not** disabled if a parent denies a change. We integrate the API
> because we serve ads and IAP, which are the touchpoints the laws actually concern.

> **Scope of the age data:** age/minor status is used **only** for (a) AdMob age-appropriate ad
> configuration and (b) compliance. It is **never** fed into analytics, profiling, or ad targeting.
> The fallback screen explicitly informs the user of this limited use.

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

- Artifact: `com.google.android.play:age-signals:0.0.3`. Min API 23 — **we are already at minSdk
  23**.
- No runtime permission needed; requires app installed/updated by Google Play (`APP_NOT_OWNED`,
  `SDK_VERSION_OUTDATED` are non-retryable).
- Call:
  `AgeSignalsManagerFactory.create(context).checkAgeSignals(AgeSignalsRequest.builder().build())`
  → `AgeSignalsResult`:
    - `userStatus()`: `VERIFIED` / `DECLARED` / `SUPERVISED` / `SUPERVISED_APPROVAL_PENDING` /
      `SUPERVISED_APPROVAL_DENIED` / `UNKNOWN` / `null` (not in scope region or not shared).
    - `ageLower()` (0–18), `ageUpper()` (2–18), `mostRecentApprovalDate()`, `installId()`.
- Retryable error codes (-1…-8, -100) → exponential backoff; cache is Play-managed (updated 2–8
  weeks after a birthday), so caching our own resolved result is acceptable.

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
| Fallback age-collection screen         | ❌ does not exist         | —                                                                |
| House "remove ads" fallback ad         | ❌ does not exist         | —                                                                |

---

## Design

### 1. New dedicated `:age` module (decoupled, Android-only for now)

Per decision: a **new standalone module** isolates the Play library from our domain, so the rest
of the app depends only on our abstraction — not on `com.google.android.play.agesignals`. Android
only now; structured so the resolver can later become `expect/actual` for KMP/iOS without touching
consumers.

```
:age
  api/                         // pure-Kotlin contract (KMP-ready, no Play imports)
    AgeSignals.kt              // domain model: userStatus, ageLower, ageUpper, isMinor, source
    AgeStatus.kt               // our enum, mapped from Play's AgeSignalsVerificationStatus
    AgeResolution.kt           // sealed: Resolved(AgeSignals) | Unavailable(reason) — drives fallback
    AgeSignalsProvider.kt      // interface: suspend fun resolve(): AgeResolution
    AgeRepository.kt           // interface: StateFlow<AgeSignals?>, refresh(), submitDeclaredAge()
  android/                     // Android/Play implementation (the only place Play SDK is imported)
    PlayAgeSignalsProvider.kt  // wraps AgeSignalsManager via suspendCancellableCoroutine
    AgeErrorMapper.kt          // AgeSignalsErrorCode → retryable/non-retryable
    AgeRepositoryImpl.kt       // resolve → cache (DataStore) → expose StateFlow
    AgePreferences.kt          // DataStore: cached AgeSignals + "age source" (api | declared)
    AgeModule.kt               // Koin
```

- `PlayAgeSignalsProvider` adapts the Play Tasks `addOnSuccessListener`/`addOnFailureListener`
  callback into a suspend call returning `AgeResolution` (`suspendCancellableCoroutine`).
- **`AgeResolution`** is the key abstraction — and it must distinguish **jurisdiction**, because the
  law (Texas SB 2420 today; more states later) only applies to covered users. The API tells us this
  directly via `userStatus`:
    - `Covered(AgeSignals)` — a real status (`VERIFIED`/`DECLARED`/`SUPERVISED`/`SUPERVISED_*`).
      User
      is in a covered jurisdiction with a known signal. Use it.
    - `CoveredAgeUnknown` — `userStatus == UNKNOWN` = **"age unknown in applicable jurisdiction"**.
      Covered (e.g. a Texan) but no age on file. → **the only case that triggers the fallback age
      screen.**
    - `NotCovered` — `userStatus == null` = **"not in applicable region"** (most users worldwide).
      → **no screen, standard ads.** We do **not** ask these users for their age.
    - `Undetermined(reason)` — an actual error (network/service/transient/non-retryable), so we got
      **neither** a status nor a definitive `null`. Jurisdiction unknown. → retry next cold start;
      prompt **only on first run if no age is stored yet** (see fallback §2).
- **Why this matters:** any **definitive** API response — a real status **or** `null` — lets us act
  and **skip the screen**. The screen is a **one-time bootstrap**: shown only on first run when the
  API gave us no usable data (`UNKNOWN` or error) and we have nothing stored. Once any age exists
  (declared or API-sourced), it is never shown again, even if the API later fails.
- **`AdTreatment` bracket** derived rule (single source of truth, computed in `:age`) — **not a
  binary minor flag**, because ad law has two thresholds (COPPA under-13 → TFCD,
  under-age-of-consent
  13–17 → TFUA). Three buckets, aligned to Play's default ranges:
    - `CHILD_DIRECTED` (under 13): `ageUpper != null && ageUpper < 13`. → TFCD.
    - `UNDER_AGE_OF_CONSENT` (13–17): minor but not under 13 — `SUPERVISED`/`SUPERVISED_APPROVAL_*`,
      or `ageUpper in 13..17`. → TFUA.
    - `STANDARD` (18+): `VERIFIED`/`DECLARED` with `ageLower >= 18`.
    - Declared via fallback screen: bracket comes straight from the user's chosen bucket (or derived
      from birth year). `isMinor` remains a convenience derived from `AdTreatment != STANDARD`.
- **`source`** field on `AgeSignals`: `API` or `DECLARED` — so we know provenance and can prefer a
  fresh API result over a stale declared one.
- Persist last resolved `AgeSignals` in DataStore so ads apply the right config immediately on next
  launch before the async call returns. Refresh on each app start (per decision — cold start only).
- Retry policy: retry retryable error codes with exponential backoff (max ~3); exhausted/
  non-retryable → `Undetermined` (jurisdiction unknown), **not** an automatic prompt.

### 2. Fallback age-collection screen (only when the law requires it)

Per decision: the screen is a **one-time bootstrap** to guarantee we always have an initial age.
Rule: **show it only on first run, when we have no stored age AND the API gave us no usable data.**

- **Single trigger:** `noStoredAge && (CoveredAgeUnknown || Undetermined)`.
    - `CoveredAgeUnknown` (`UNKNOWN`) — covered jurisdiction, age missing.
    - `Undetermined` (API error) — jurisdiction unknown; on first run we still collect, regardless
      of
      country (acceptable per decision — collecting once for everyone is fine).
- **`NotCovered` (`null`) → never show the screen**, even on first run. A `null` is a definitive
  answer ("not covered"); we store that we resolved and serve standard ads.
- **`Covered` (real status) → never show**; use the API value.
- **Once any age is stored (declared OR API), never show again** — including when the API later
  fails. A subsequent definitive API result still supersedes a declared value (updates the bracket),
  but does not re-trigger the screen.
- **Collected once, persisted.** One-time collection is sufficient for compliance — we do **not**
  re-prompt every launch. Once stored (`source = DECLARED`), the gate is satisfied. The user can
  change it later via a **Settings option** (see §6). The API still re-resolves per cold start; only
  the declared fallback is one-time.
- **Placement in startup flow (per decision):** the gate sits **after consent (legal + tracking),
  before onboarding**. Existing flow gates on `consentCompleted` → `onboardingCompleted`
  (`MainViewModel.kt:38`); insert an `ageDecided` gate between them. Add `ageDecided` to
  `MainState.Ready`, resolved from the `:age` repo. **`ageDecided` is true** whenever any age is
  stored OR the API returned a definitive result (`Covered` or `NotCovered`); it is **false only**
  on
  first run with no stored age and a non-definitive API result (`CoveredAgeUnknown`/`Undetermined`),
  per §2.
    - **Recovery rule:** if consent is already accepted (screen not re-shown) **and** onboarding is
      complete **but** `ageDecided` is still false → show the age screen anyway. The gate is "age
      not
      decided", evaluated independently of where the user is in the flow.
- **Purchased users:** since age is used **only** for ads, if ads are already removed via purchase
  there is no age-dependent behavior left — **skip the screen entirely**.
- **UX / content (per decision — birth year):**
    - Collect **birth year**; derive the `AdTreatment` bucket from it (`<13` → CHILD_DIRECTED,
      `13–17` → UNDER_AGE_OF_CONSENT, `18+` → STANDARD) — enough to set TFCD vs TFUA vs standard.
    - Clear disclosure: this age is used **only** to keep ads age-appropriate and for legal
      compliance; it is not shared, profiled, or used for targeting.
    - Persist via `AgeRepository.submitDeclaredAge()` → cached with `source = DECLARED`.
- **Module placement:** the screen UI lives in `:compose` (our UI layer / design system), driven by
  an MVI ViewModel that talks to `:age`'s `AgeRepository`. `:age` stays UI-agnostic.

### 3. AdMob age-appropriate configuration

In `DrinkWaterAppAds` (or a new `AdsConfigurator`), before/around `MobileAds.initialize`, apply:

```kotlin
val config = RequestConfiguration.Builder()
    .setTagForChildDirectedTreatment(            // COPPA, under 13
        if (treatment == CHILD_DIRECTED) TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
        else TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
    )
    .setTagForUnderAgeOfConsent(                 // under age of consent, 13–17
        if (treatment == UNDER_AGE_OF_CONSENT) TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
        else TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED
    )
    .build()
MobileAds.setRequestConfiguration(config)
```

- AdMob has **two** minor configs (per Q1): `TAG_FOR_CHILD_DIRECTED_TREATMENT` (TFCD, COPPA / under
    13) and `TAG_FOR_UNDER_AGE_OF_CONSENT` (TFUA, 13–17). We drive both off the `AdTreatment`
        bracket.
        Setting either makes Google serve only age-appropriate / non-personalized ads. We still
        serve ads
        (per decision); the bracket comes from API **or** declared fallback — same code path, single
        source.
- Since `MobileAds.initialize` runs once at startup but age resolves async, **re-apply
  `setRequestConfiguration` once age is resolved** (it affects subsequent ad requests). Banner
  requests made after resolution pick up the TFUA flag.

### 4. Banner placeholder + house "remove ads" fallback

Per decision:

- Always keep the **banner placeholder** slot (existing behavior in `DrinkWaterBannerAd`).
- When no fillable/age-appropriate ad is returned, render a **default house ad** promoting the
  "Remove ads" purchase (reuse the existing `RemoveAdsBottomSheet` entry point on tap).
- This house fallback is a UI composable in `:ads` (`RemoveAdsHouseAd.kt`) shown on the banner's
  failed/empty state (`DrinkWaterBannerAdStates`).
- If the app **was purchased** (`PurchaseState` = removed) → **no ads at all**, no placeholder,
  no house ad. (Unchanged precedence; purchase wins over everything.)

Precedence: `purchased → render nothing` › `ad available → render ad (TFUA-aware)` ›
`no ad → render remove-ads house ad in placeholder`.

### 5. Play Console / operational (non-code)

- Add **age ratings to the RevenueCat "remove ads" SKU** in Play Console (or Publishing API).
- Complete the **Age signals** Play Console section; optionally configure custom age ranges
  (defaults 0-12 / 13-15 / 16-17 / 18+ are fine — likely no change).
- Assess whether any app behavior is a **"significant change"** requiring parental approval. For a
  water-reminder app this is likely **none**, but document the assessment and the Console
  declaration path.
- Set up monitoring of **parental-approval revocation reports**.
- Confirm the **`.dev`/`.staging` flavors** behavior — Age Signals returns `APP_NOT_OWNED` for
  sideloaded/non-Play installs, so dev builds hit `Undetermined` → on first run with no stored age
  the bootstrap screen shows (then never again). Provide a `FakeAgeSignalsManager`
  (`com.google.android.play.agesignals.testing`) wiring for tests.

### 6. Settings: change declared age

- Add a Settings entry to **view/change the declared age bracket** (the one-time fallback value).
- Editable **only** when the age is `DECLARED` (user-provided). When age is `API`-sourced, show it
  read-only (or hide), since the API is authoritative and we must not let users override the signal.
- Writing a new value updates `:age` (`submitDeclaredAge`) and re-applies the AdMob
  `RequestConfiguration` so the new bracket takes effect on subsequent ad requests.

---

## Implementation steps

1. Create the `:age` module (Gradle, convention plugins, version-catalog entry for
   `com.google.android.play:age-signals:0.0.3`); register in `settings.gradle`.
2. Build `:age` `api/` contract (model, `AgeStatus`, `AgeResolution`, `AgeSignalsProvider`,
   `AgeRepository`) — pure Kotlin, no Play imports.
3. Build `:age` `android/` impl (`PlayAgeSignalsProvider`, error mapper, `AgeRepositoryImpl`,
   `AgePreferences` DataStore, Koin module); register module in the Application `startKoin`.
4. Resolve age on app start; persist + expose `StateFlow<AgeSignals?>` and an `AgeResolution`
   signal the UI can observe to decide whether to show the fallback screen.
5. Build the **bootstrap age-collection screen** in `:compose` (MVI ViewModel + composable +
   birth-year input + disclosure copy); derive bracket and write back to `:age`.
6. Wire the **age gate into the startup flow**: add `ageDecided` to `MainState.Ready`
   (`MainViewModel.kt:38`), gate **after consent, before onboarding**, with the recovery rule. Gate
   fires **only** on first run with no stored age and a non-definitive API result
   (`CoveredAgeUnknown`/`Undetermined`); `NotCovered`/`Covered`/any stored age pass through; skip if
   ads purchased.
7. Wire `AdTreatment` into ad serving: set **both** TFCD and TFUA on `RequestConfiguration` in
   `DrinkWaterAppAds`, re-applied after resolution (identical for API-sourced and declared age).
8. Build the **house "remove ads" fallback ad** for the banner empty state; keep placeholder;
   keep purchased = no-ads precedence.
9. Add **Settings entry** to view/change the declared age (editable only when `source = DECLARED`);
   re-apply ad config on change.
10. Tests:
    - `:age` repository: `null` → `NotCovered`; `UNKNOWN` → `CoveredAgeUnknown`; real statuses →
      `Covered` + bracket; errors → `Undetermined`; retry on retryable codes; declared-age write;
      API supersedes declared. Via `FakeAgeSignalsManager`.
    - Age-gate logic (fires only on first run + no stored age + `CoveredAgeUnknown`/`Undetermined`;
      `NotCovered`/`Covered`/any stored age never prompts; recovery rule; skipped when purchased;
      not re-shown after a later API failure).
    - Ads decision: under-13 → TFCD; 13–17 → TFUA; 18+ → neither; purchased → nothing; empty fill →
      house ad.
11. Play Console: SKU age ratings, Age signals section, significant-change assessment doc,
    revocation report monitoring.
12. (Optional hardening) Pair with **Play Integrity API** to verify untampered caller.

---

## Resolved decisions

1. **One-time bootstrap, scoped by the API** → the screen is shown **only on first run** when we
   have no stored age and the API gave no usable data (`UNKNOWN` or error). Any definitive API
   response — real status **or** `null` (not covered) — skips it. Once any age is stored (declared
   or
   API), it is never shown again, even if the API later fails. Disclosure says age is used only for
   age-appropriate ads + compliance; a later definitive API result supersedes a declared value. Age
   affects **ads only**. *(Resolved.)*
2. **Module placement** → a **new dedicated `:age` module**, isolating the Play library from our
   domain. Android-only now; structured for later KMP `expect/actual` iOS support. *(Resolved.)*
3. **Refresh cadence** → **cold start only** for now. Play caches server-side; per-launch suffices.
   *(Resolved.)*
4. **iOS** → out of scope; Apple has its own Declared Age Range API. Deferred to a future plan,
   enabled by the `:age` `api/` contract staying Play-free. *(Resolved / future.)*
5. **Input granularity** → **birth year**, deriving the bracket (`<13` → TFCD, `13–17` → TFUA,
   `18+` → standard). A binary under-18/18+ toggle is **not** sufficient: ad compliance has two
   thresholds — COPPA (under 13) and under-age-of-consent (13–17) — so we set both AdMob tags from
   the derived bracket. *(Resolved.)*
6. **Collection frequency** → **once, persisted** — sufficient for compliance; no per-launch
   re-prompt. Editable later via a Settings option. The API still re-resolves per cold start.
   *(Resolved.)*
7. **Gate placement** → **after consent (legal + tracking), before onboarding**, via an `ageDecided`
   gate in `MainState`. **Recovery rule:** if consent already accepted and onboarding complete but
   age still unset → show the age screen. Skipped entirely when ads are purchased. *(Resolved.)*

8. **Hard API failure (`Undetermined`)** → **no region guard.** On first run with no stored age, we
   collect age regardless of country (a one-time prompt for everyone is acceptable). On later
   failures, if any age is already stored we do **not** re-prompt. *(Resolved.)*