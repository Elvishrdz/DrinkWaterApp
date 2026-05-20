# Plan: Updates & Corrections (2026-05-20)

## Context

Following the 15-task implementation batch, several fixes and new requirements were identified:
locale detection improvement, onboarding step reordering, stats decimal formatting, dialog text
correction, onboarding back navigation, a full-screen water animation replacement, and a redesigned
inline stepper edit UX.

---

## Tasks

### A — T09: Proper measurement system detection via ICU
**File:** `compose/src/main/java/com/eahm/drinkwaterapp/core/data/datastore/DataStorePrefsStorage.kt`

Replace the current `Locale.getDefault().country == "US"` check with
`android.icu.util.LocaleData.getMeasurementSystem(ULocale.getDefault())`.
Available from API 24 (minSdk = 24), no version guard needed.

```kotlin
import android.icu.util.LocaleData
import android.icu.util.ULocale

private fun localeDefaultUnitPreference(): UnitPreference {
    val system = LocaleData.getMeasurementSystem(ULocale.getDefault())
    return when (system) {
        LocaleData.MeasurementSystem.US -> UnitPreference.FL_OZ
        else -> UnitPreference.ML   // SI and UK both metric for volume
    }
}
```

Remove the `import java.util.Locale` that was added in the previous session.

---

### B — T11: Move Units step to position 2 (first after Intro) + neutral fl oz description
**Files:**
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/onboarding/OnboardingState.kt`
- `compose/src/main/res/values/strings.xml`
- `compose/src/main/res/values-es/strings.xml`

**Step order change:**
```
1 → Intro
2 → Units        (was 3)
3 → DailyGoal    (was 2)
4 → StartTime    (unchanged)
5 → EndTime
6 → Interval
7 → Notification
8 → ExactAlarm
9 → Celebrations
10 → Summary
```

Update `byStep()` in `OnboardingState.kt` to reflect the new mapping.

**String updates (EN + ES):**
- `onb_units_fl_oz_subtitle`: Change "Common in the US" → "Used to measure liquids and beverages"
  (ES: "Usado para medir líquidos y bebidas")

---

### C — Stats average: 1 decimal or integer via `sanitizeZeroDecimals`
**File:** `compose/src/main/java/com/eahm/drinkwaterapp/core/domain/extension/Float.kt`

Change the non-integer branch of `sanitizeZeroDecimals()`:

```kotlin
fun Float.sanitizeZeroDecimals(): String {
    return if (this % 1f == 0f) this.toInt().toString()
    else "%.1f".format(this)   // was: this.toString()
}
```

This propagates to `formatVolume()` → stats `SummaryCard` average display, and is safe globally
since all other float displays (stepper values) are whole numbers.

---

### D — T12: Fix first drink dialog body text
**Files:**
- `compose/src/main/res/values/strings.xml`
- `compose/src/main/res/values-es/strings.xml`

Update `first_drink_dialog_body`:
- **EN:** `"Your drink has been added to today\'s log.\n\nMade a mistake? Go to the History screen and delete the entry from there."`
- **ES:** `"Tu bebida ha sido agregada al registro de hoy.\n\n¿Fue un error? Puedes ir a la pantalla de Historial y eliminar la entrada desde ahí."`

---

### E — Onboarding BackHandler
**File:** `compose/src/main/java/com/eahm/drinkwaterapp/feature/onboarding/OnboardingScreen.kt`

Add a system back press handler inside `OnboardingScreen`. `prevStep()` already exists in the
ViewModel and clamps at step 1. When on step 1 (Intro), `BackHandler` is disabled so the system
default (finish/close Activity) applies.

```kotlin
import androidx.activity.compose.BackHandler

// inside OnboardingScreen, before the CompositionLocalProvider:
BackHandler(enabled = state.step > 1) {
    viewModel.prevStep()
}
```

---

### F — T13: Full-screen rising water animation
**Files to modify:**
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/drink/ui/WaterRisingAnimation.kt` — full rewrite
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/drink/DrinkRoot.kt` — add animation layer
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/drink/DrinkScreen.kt` — remove old animation from progress ring Box
- `compose/src/main/java/com/eahm/drinkwaterapp/feature/drink/DrinkViewModel.kt` — restrict trigger to suggestion sources
- Dev menu file for toggle (follow existing dev menu toggle pattern)

#### Animation design

`WaterRisingAnimation.kt` rewrite using a `WaterRisingConfig` data class:

```kotlin
data class WaterRisingConfig(
    val riseMs: Long = 1_500,
    val holdMs: Long = 300,
    val fadeMs: Long = 500,
    val waveAmplitudeDp: Float = 8f,
    val waveFrequency: Float = 1.5f,
    val waveSpeed: Float = 3.5f,
    val bubbleCount: Int = 8,
    val waterColor: Color = DwWater,
    val bubbleColor: Color = Color.White.copy(alpha = 0.35f),
)
```

Canvas animation phases (frame-by-frame with `withFrameMillis`):
1. **Rise phase** (0ms → riseMs): `fillFraction` goes 0f → 1f. Draw blue rect from bottom up,
   sine-wave edge on top of water, rising bubbles within water area. Wave phase driven by elapsed time.
2. **Hold phase** (riseMs → riseMs+holdMs): full blue, bubbles continue.
3. **Fade phase** (riseMs+holdMs → total): `alpha` 1f → 0f. Composable disappears revealing DrinkScreen.

The composable has `alpha = 0f` at rest and is pointer-input disabled so it never intercepts touches
when invisible.

#### Placement in DrinkRoot.kt

```kotlin
Box(Modifier.fillMaxSize()) {
    DrinkScreen(...)          // rendered first = visual bottom
    if (data.showFirstDrinkDialog) FirstDrinkDialog(...)
    WaterRisingAnimation(     // rendered last = visual top
        trigger = data.drinkAnimationTrigger,
        modifier = Modifier.fillMaxSize(),
    )
}
```

`fillMaxSize` without inset-padding means the animation fills behind status/nav bars.

#### Trigger restriction in DrinkViewModel.onDrink()

Only increment the trigger for suggestion sources:

```kotlin
if (source == DrinkSource.QUICK_SUGGESTION || source == DrinkSource.BOTTOM_SHEET_SUGGESTION) {
    _drinkAnimationTrigger.value += 1
}
```

Custom amount bottom sheet does NOT trigger the animation.

#### Dev menu toggle

Add a boolean preference `WATER_ANIMATION_ENABLED` (default `true`) accessible from the dev menu.
Check it in `DrinkViewModel` before incrementing the trigger. Follow the existing dev menu toggle
pattern in the codebase.

---

### G — T15: Inline tap-to-edit stepper (replaces always-visible text field)

Remove the `OutlinedTextField` added in the previous T15 implementation from all three bottom sheets,
then add inline tap-to-edit to all stepper usages.

**Pattern:**
Each stepper center value `Text` becomes `clickable`. Tapping it sets `isEditing = true`.
`AnimatedContent(isEditing)` switches between:
- **Normal view**: `[−] [value Text (clickable)] [+]`
- **Editing view**: `[OutlinedTextField]` + Row with `[Cancel] [Save]`

Cancel → `isEditing = false`, clear text input, value unchanged.
Save → parse + validate text, if valid call `onValueSet(parsed)`, `isEditing = false`. If invalid,
show an inline error message below the field (do not close editing mode).

No outer Save button coordination needed — the inline Save is self-contained.

#### Input validation (per stepper)

Reuse the existing constants already defined in each bottom sheet file and follow the same
`toFloatOrNull()` / `toIntOrNull()` + range-check pattern from `AmountInputField.kt`:

| Stepper | Parse as | Min | Max | Error behaviour |
|---------|----------|-----|-----|-----------------|
| Goal (GoalBottomSheet / StepSetGoal) | `Float` | `GOAL_MIN` (100f) | `GOAL_MAX` (100_000f) | show error text, stay in editing mode |
| Interval (IntervalBottomSheet) | `Int` | existing `STEP_MIN` | existing `STEP_MAX` | show error text, stay in editing mode |
| Notif amount (NotificationAmountBottomSheet / StepNotifs) | `Int` | `AMOUNT_MIN` (50) | `AMOUNT_MAX` (99999) | show error text, stay in editing mode |

Validation steps for each Save tap:
1. `toFloatOrNull()` / `toIntOrNull()` — if null, show "Invalid number" error.
2. Range check against min/max — if out of range, show e.g. "Must be between X and Y".
3. If valid → call `onValueSet(parsed)`, `isEditing = false`, clear error.

#### Files affected

| File | Change |
|------|--------|
| `GoalBottomSheet.kt` | Remove OutlinedTextField below stepper; add tap-to-edit to `GoalStepperRow` (adds `onValueSet: (Float) -> Unit` param) |
| `StepSetGoal.kt` | Add `onValueSet = { currentGoal = it; onGoalChanged(it) }` to `GoalStepperRow` call |
| `IntervalBottomSheet.kt` | Remove OutlinedTextField; add tap-to-edit inline to the stepper Row |
| `NotificationAmountBottomSheet.kt` | Remove OutlinedTextField from `NotificationAmountSheetContent`; add tap-to-edit to its stepper Row |
| `StepNotifs.kt` | Add tap-to-edit to private `AmountStepper` composable (add `onValueSet: (Int) -> Unit` param; `NotifsConfigContent` passes callback to `AmountStepper`) |

Also remove now-unused imports (`OutlinedTextField`, `KeyboardActions`, `KeyboardOptions`,
`mutableStateOf` if only used for textInput, `LocalFocusManager`, `ImeAction`, `KeyboardType`)
from each file where they were added solely for the old T15 implementation.

---

## Commit plan

| # | Commit message |
|---|----------------|
| A | `T09: use ICU LocaleData.getMeasurementSystem for unit locale detection` |
| B | `T11: move units selection to step 2 and improve fl oz description` |
| C | `fix stats average display to show 1 decimal or integer via sanitizeZeroDecimals` |
| D | `T12: fix first drink dialog text — remove swipe instruction` |
| E | `onboarding: handle system back gesture to navigate previous steps` |
| F | `T13: replace water animation with full-screen rising wave animation` |
| G | `T15: replace static text field with inline tap-to-edit stepper` |

---

## Verification checklist

- **A**: Change device locale to UK → default unit is ML; US locale → FL_OZ.
- **B**: Fresh install onboarding shows Units at step 2 (after Intro). fl oz subtitle shows no country name.
- **C**: Stats screen average shows e.g. "1537.3 mL" or "2000 mL" (never more than 1 decimal).
- **D**: Log first drink on clean install → dialog shows corrected body text with no swipe instruction.
- **E**: In onboarding at step 3+, pressing system back returns to previous step. At step 1, back closes app.
- **F**: Tap a quick preset button → full-screen water rises from bottom, fills blue with wave + bubbles, fades out. Custom amount (bottom sheet) does NOT trigger animation. Dev menu toggle disables animation.
- **G**: In all steppers (goal, interval, notif amount — both settings and onboarding): tapping center value shows inline text field + Cancel/Save with animation; typing and Save sets the value; Cancel restores normal view.
