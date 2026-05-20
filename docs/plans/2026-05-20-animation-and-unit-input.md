# Plan: Animation Fixes & Unit-Aware Input Field (2026-05-20)

## Context

Two follow-up task groups after the previous implementation batch:
1. **Animation fixes** — two UX corrections to the full-screen water rising animation.
2. **Unit-aware preset input** — the custom-amount input field should accept and validate
   values in the user's chosen unit, not raw mL.

---

## Group A — Animation Fixes

### A1: Water rises slightly above screen top

**Problem:** When `fillFraction` reaches 1.0, `waterTopY = size.height * (1 - 1.0) = 0` — the
wave sits exactly at the top edge of the canvas. It never visually exits the screen, making the
animation feel like it stops before completion.

**Fix:** Subtract a fixed overflow offset from `waterTopY` so the water surface rises a few dp
above y=0. The canvas clips naturally at screen edges, so nothing renders outside the screen.

**File:** `WaterRisingAnimation.kt`

1. Add `overflowDp: Float = 24f` to `WaterRisingConfig`.
2. Compute `overflowPx` alongside `waveAmplitudePx`:
   ```kotlin
   val overflowPx = with(density) { config.overflowDp.dp.toPx() }
   ```
3. Change the waterTopY calculation in the Canvas block:
   ```kotlin
   val waterTopY = size.height * (1f - fillFraction) - overflowPx
   ```

The bubble guard `if (by >= waterTopY)` already uses `waterTopY`, so bubbles clip correctly
without any other changes.

---

### A2: Animation plays once per drink — no mid-play restarts, no replay on re-entry

**Requirements:**
- If the user taps a drink button while the animation is already playing, it must **not** restart.
  The current animation completes in full; only a drink logged **after** completion triggers a new one.
- Re-entering the Drink screen (navigate away → back) must **not** replay the animation.

**Root cause of both issues:** The current `_drinkAnimationTrigger: MutableStateFlow<Int>` increments
on every drink. `LaunchedEffect(trigger)` restarts whenever `trigger` changes, so rapid taps restart
the animation mid-play. And when the composable is recreated after navigation, `LaunchedEffect`
re-runs with the stored non-zero trigger.

**Fix:** Replace the `Int` trigger with a `Boolean` flag `_animationPending`.

- `LaunchedEffect(playing: Boolean)` only has two possible keys (`true`/`false`).
- While playing (`playing = true`), further drinks do **not** change the value → `LaunchedEffect`
  does not restart ✓
- When the animation finishes, `onAnimationDone()` sets it back to `false` ✓
- On screen re-entry, `playing = false` → `LaunchedEffect(false)` → early return ✓
- The next drink after completion sets it back to `true` → new animation plays ✓

**Files:** `DrinkState.kt`, `DrinkViewModel.kt`, `WaterRisingAnimation.kt`, `DrinkRoot.kt`

#### DrinkState.kt
Replace `drinkAnimationTrigger: Int = 0` with `drinkAnimationPending: Boolean = false`.

#### DrinkViewModel.kt
Replace `_drinkAnimationTrigger = MutableStateFlow(0)` with
`_animationPending = MutableStateFlow(false)`.

In `onDrink()`, only set to `true` if it is currently `false` (already-playing animation is
preserved):
```kotlin
val animEnabled = prefsStorage.getWaterAnimationEnabled().first()
if (animEnabled &&
    !_animationPending.value &&
    (source == DrinkSource.QUICK_SUGGESTION || source == DrinkSource.BOTTOM_SHEET_SUGGESTION)
) {
    _animationPending.value = true
}
```

Add the callback:
```kotlin
fun onAnimationDone() {
    _animationPending.value = false
}
```

Update the `combine` chain: replace `_drinkAnimationTrigger` with `_animationPending`, mapping
`drinkAnimationPending = pending`.

#### WaterRisingAnimation.kt
Change signature from `trigger: Int` to `playing: Boolean`.
Change `LaunchedEffect(trigger)` to `LaunchedEffect(playing)`.
Change early-return guard from `if (trigger == 0) return@LaunchedEffect` to
`if (!playing) return@LaunchedEffect`.
Add `onAnimationDone: () -> Unit = {}` parameter. Call it at the end of the animation loop:
```kotlin
else -> {
    animAlpha = 0f
    fillFraction = 0f
    onAnimationDone()
    break
}
```

#### DrinkRoot.kt
```kotlin
WaterRisingAnimation(
    playing = data.drinkAnimationPending,
    onAnimationDone = viewModel::onAnimationDone,
    modifier = Modifier.fillMaxSize(),
)
```

---

## Group B — Unit-Aware Preset Input Field

### Goal

The custom-amount `OutlinedTextField` in `InputFieldArea` (`AddDrinkButton.kt`) currently:
- Labels itself with a static string (`drink_custom_amount_label`), always implying mL.
- Validates against a fixed mL range (0 < value ≤ 99999).
- Passes the raw parsed float directly to `onCreatePreset(Float)` and `onDrink(Float)`.

Users whose `UnitPreference` is `FL_OZ` or `AUTO_LITERS` must mentally convert before typing.
The field should accept input in the user's display unit and convert to mL internally.

### Effective display units

| `UnitPreference` | Display unit | Notes |
|-----------------|--------------|-------|
| `ML`            | mL           | No change from current behaviour |
| `FL_OZ`         | fl oz        | Fixed unit, single input field |
| `AUTO_LITERS`   | mL *or* L    | User toggles between sub-units; show a segmented toggle |

`AUTO_LITERS` requires a sub-unit toggle because the user might want to enter "500 mL" or "1.5 L"
depending on the amount. The toggle only appears when `AUTO_LITERS` is active.

### Validation limits by display unit

| Display unit | Min    | Max      | Equivalent mL range |
|-------------|--------|----------|---------------------|
| mL          | 1      | 99 999   | 1 – 99 999 mL |
| fl oz       | 0.1    | 3 381.0  | ≈ 3 – 99 999 mL (use `99999f / 29.574f` for max) |
| L           | 0.001  | 99.999   | 1 – 99 999 mL |

### Conversion on submit (display unit → mL)

```kotlin
val amountMl: Float = when (effectiveUnit) {
    SubUnit.ML    -> parsed
    SubUnit.L     -> parsed * 1000f
    SubUnit.FL_OZ -> parsed * 29.574f
}
```

`29.574f` is the same constant already used in `Float.kt` `formatVolume()`.

### Layout

**Non-AUTO_LITERS (ML, FL_OZ):**
```
[OutlinedTextField — "Amount (mL)" or "Amount (fl oz)"]
[Create]  [Drink]
```

**AUTO_LITERS — any sub-unit:**
```
Row: [OutlinedTextField  weight=1f]  [Spacer 8dp]  [SingleChoiceSegmentedButtonRow: mL | L]
[Create]  [Drink]
```

The toggle is only rendered when `unitPreference == AUTO_LITERS`.

**Theming:** All components must use `MaterialTheme` — `MaterialTheme.colorScheme` for colors,
`MaterialTheme.shapes` for corner radii. No hardcoded colors or shapes in the new toggle widget.

### Internal state inside `InputFieldArea`

```kotlin
// Scoped to AUTO_LITERS only; ignored for ML and FL_OZ
private enum class SubUnit { ML, L, FL_OZ }

var selectedSubUnit by remember(unitPreference) {
    mutableStateOf(
        when (unitPreference) {
            UnitPreference.FL_OZ -> SubUnit.FL_OZ
            else -> SubUnit.ML
        }
    )
}

val effectiveUnit = when (unitPreference) {
    UnitPreference.ML          -> SubUnit.ML
    UnitPreference.FL_OZ       -> SubUnit.FL_OZ
    UnitPreference.AUTO_LITERS -> selectedSubUnit  // mL or L, controlled by toggle
}

val (label, minVal, maxVal) = when (effectiveUnit) {
    SubUnit.ML    -> Triple(stringResource(R.string.drink_custom_amount_label_ml),    1f,     99_999f)
    SubUnit.FL_OZ -> Triple(stringResource(R.string.drink_custom_amount_label_fl_oz), 0.1f,   99_999f / 29.574f)
    SubUnit.L     -> Triple(stringResource(R.string.drink_custom_amount_label_l),     0.001f, 99.999f)
}
```

When `selectedSubUnit` changes, clear `customAmount` so the user starts fresh and avoids silently
carrying over an mL value interpreted as L.

### Files affected

| File | Change |
|------|--------|
| `AddDrinkButton.kt` | Pass `unitPreference` to `InputFieldArea` |
| `AddDrinkButton.kt` — `InputFieldArea` | Add `unitPreference` param; `SubUnit` enum; `selectedSubUnit` state; dynamic label; themed segmented toggle for AUTO_LITERS; unit-converted validation + submit |
| `strings.xml` | Add `drink_custom_amount_label_ml`, `drink_custom_amount_label_l`, `drink_custom_amount_label_fl_oz` |
| `values-es/strings.xml` | Same keys in Spanish |

### String resources

`strings.xml`:
```xml
<string name="drink_custom_amount_label_ml">Amount (mL)</string>
<string name="drink_custom_amount_label_l">Amount (L)</string>
<string name="drink_custom_amount_label_fl_oz">Amount (fl oz)</string>
```

`values-es/strings.xml`:
```xml
<string name="drink_custom_amount_label_ml">Cantidad (mL)</string>
<string name="drink_custom_amount_label_l">Cantidad (L)</string>
<string name="drink_custom_amount_label_fl_oz">Cantidad (fl oz)</string>
```

The original `drink_custom_amount_label` string is kept (still used by `AppInputField` elsewhere).

---

## Commit plan

1. `fix: water animation overflows screen top with configurable overflow offset`
2. `fix: water animation is one-shot — no mid-play restarts, no replay on screen re-entry`
3. `feat: unit-aware custom amount input — accept fl oz and L, convert to mL on submit`

---

## Verification

- **A1:** Tap a quick preset. The blue water fills past the top edge of the screen (wave exits
  above y=0) before fading out. No visible "ceiling" where water stops and then fades.
- **A2 — no restart:** Tap a quick preset rapidly 3 times in quick succession. The animation
  plays through exactly once without restarting. After it finishes, the next drink plays it again.
- **A2 — no replay:** Log a drink (animation plays). Navigate to History. Return to Drink screen.
  Animation does not replay.
- **B (FL_OZ):** Switch unit to fl oz. Open preset bottom sheet. Label reads "Amount (fl oz)".
  Type "10". Tap Drink. Verify DB entry is ~295.7 mL.
- **B (AUTO_LITERS mL):** Switch unit to Auto (L). Toggle `mL | L` is shown; mL is selected by
  default. Type "500". Drink. DB entry = 500 mL.
- **B (AUTO_LITERS L):** Toggle to L. Type "1.5". Drink. DB entry = 1500 mL.
- **B (sub-unit switch clears input):** Type "500" in mL mode, toggle to L — field clears.
- **B validation:** fl oz mode, type "0" → error. Type "99999" → error (exceeds fl oz max ~3381).
  Type "16" → valid, submits as ~473 mL.
