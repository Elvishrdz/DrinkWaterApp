# Plan: Unit Fixes, History Dev Menu & Stats Graph (2026-05-20)

---

## Task 1 — Fix daily goal and notification amount display in Settings

### Root cause

`GoalBottomSheet` and `NotificationAmountBottomSheet` are invoked in `SettingsScreen.kt`
**without** passing `unitPreference`, so both fall back to the default `UnitPreference.ML`.
The row labels call `formatVolume(state.unitPreference)` correctly, but the sheets always
display and validate in mL.

### Fix — `SettingsScreen.kt`

Add `unitPreference = state.unitPreference` to both call sites:

```kotlin
GoalBottomSheet(
    goalMl = state.dailyGoalMl,
    onGoalChanged = onGoalChanged,
    onDismiss = { showGoalSheet = false },
    unitPreference = state.unitPreference,   // ← add
)

NotificationAmountBottomSheet(
    currentAmountMl = state.notificationAmountMl,
    onAmountChanged = onNotificationAmountChanged,
    onDismiss = { showNotificationAmountSheet = false },
    unitPreference = state.unitPreference,   // ← add
)
```

---

## Task 2 — Unit-aware inline validation in Goal and Notification Amount steppers

### Problem

Both steppers' `trySave()` functions validate the typed text against raw mL constants
(`GOAL_MIN = 100`, `GOAL_MAX = 100_000`, `AMOUNT_MIN = 50`, `AMOUNT_MAX = 99_999`).

When `unitPreference = FL_OZ` and the displayed value is "67.6 fl oz" (= 2000 mL), the user
taps to edit and types "67" — which fails `parsed < GOAL_MIN (100)`. Same issue for L mode.

### Fix scope

Four files share this pattern, all need the same change:

| File | Stepper | Constants |
|------|---------|-----------|
| `GoalBottomSheet.kt` — `GoalStepperRow` | Goal | `GOAL_MIN = 100f`, `GOAL_MAX = 100_000f` |
| `StepSetGoal.kt` — via `GoalStepperRow` | Goal (onboarding) | same |
| `NotificationAmountBottomSheet.kt` — `NotificationAmountSheetContent` | Notif amount | `AMOUNT_MIN = 50`, `AMOUNT_MAX = 99_999` |
| `StepNotifs.kt` — `AmountStepper` | Notif amount (onboarding) | same |

### Fix — effective unit helpers

Determine the "active display unit" at the time the user types:

```kotlin
// For goal: value >= 1000 && AUTO_LITERS → displayed as L; otherwise mL or fl oz
private fun effectiveDisplayUnit(pref: UnitPreference, valueMl: Float): DisplayUnit = when (pref) {
    UnitPreference.FL_OZ -> DisplayUnit.FL_OZ
    UnitPreference.AUTO_LITERS -> if (valueMl >= 1000f) DisplayUnit.L else DisplayUnit.ML
    UnitPreference.ML -> DisplayUnit.ML
}

private enum class DisplayUnit { ML, L, FL_OZ }
```

Use this in `trySave()`:

```kotlin
val displayUnit = effectiveDisplayUnit(unitPreference, value)

val (minDisplay, maxDisplay) = when (displayUnit) {
    DisplayUnit.ML    -> Pair(GOAL_MIN, GOAL_MAX)
    DisplayUnit.FL_OZ -> Pair(GOAL_MIN / 29.574f, GOAL_MAX / 29.574f)
    DisplayUnit.L     -> Pair(GOAL_MIN / 1000f, GOAL_MAX / 1000f)
}
```

### Decimal precision — preventing round-trip divergence

**Problem:** `formatVolume` rounds fl oz and L to 1 decimal place. If a user types "8.53 fl oz":
- stored as `8.53 × 29.574 = 252.37 mL`
- displayed back as `"%.1f".format(252.37 / 29.574)` = `"8.5 fl oz"` ← differs from input

The user sees a different value after saving, which is confusing.

**Fix: normalise the parsed input to the same precision as `formatVolume` BEFORE converting to mL.**

```kotlin
// Round to the precision that formatVolume uses for this unit, so the stored mL
// value always round-trips back to exactly what the user typed (at display precision).
val normalisedDisplay: Float = when (displayUnit) {
    DisplayUnit.ML    -> parsed.roundToInt().toFloat()      // mL shown as integer
    DisplayUnit.FL_OZ -> (parsed * 10).roundToInt() / 10f  // 1 decimal place
    DisplayUnit.L     -> (parsed * 10).roundToInt() / 10f  // 1 decimal place
}

val amountMl = when (displayUnit) {
    DisplayUnit.ML    -> normalisedDisplay
    DisplayUnit.FL_OZ -> normalisedDisplay * 29.574f
    DisplayUnit.L     -> normalisedDisplay * 1000f
}
onValueSet(amountMl)
```

Examples after fix:
- "8.53 fl oz" → normalised "8.5 fl oz" → stored 251.379 mL → displayed "8.5 fl oz" ✓
- "1.52 L"    → normalised "1.5 L"     → stored 1500 mL   → displayed "1.5 L" ✓
- "500.7 mL"  → normalised "501 mL"    → stored 501 mL    → displayed "501 mL" ✓

The remaining float error after normalisation is always < 0.05 in the display unit, which
`%.1f` absorbs by rounding, so the displayed value is guaranteed to match what was typed
(at the precision `formatVolume` uses).

### Text field label and unit hint

The `OutlinedTextField` label should reflect the active unit and precision so the user knows
exactly what to type:

```kotlin
label = { Text("${stringResource(R.string.settings_stepper_custom_hint)} (${displayUnit.label})") }
```

Add `val label: String` to `DisplayUnit`:
```kotlin
enum class DisplayUnit(val label: String) { ML("mL"), L("L"), FL_OZ("fl oz") }
```

> **Note:** The `effectiveDisplayUnit` helper, `DisplayUnit` enum, and `normalisedDisplay`
> logic can be placed in a shared `UnitConversionUtils.kt` inside `core/domain/extension/`
> since all four files need them.

### Onboarding note

`StepSetGoal` and `StepNotifs` already receive `unitPreference` via their composable parameters.
No ViewModel changes needed — only the `trySave()` logic and label inside the composable.

---

## Task 3 — History Inject dev menu screen

### Overview

New dev destination `HISTORY_INJECT` ("History" in the nav bar) with three sections:

1. **Bulk record editor** — compose multiple drink entries for various past dates/times and
   insert them all at once.
2. **Clear all** — delete every record in `HistoryEntity`.
3. **Clear by day** — select a date and delete all records for that day.

### Files to create

```
feature/dev/historyinject/
├── HistoryInjectDevRoot.kt
├── HistoryInjectDevScreen.kt
├── HistoryInjectDevViewModel.kt
└── HistoryInjectDevState.kt
```

### State model

```kotlin
// A single editable row in the bulk editor
data class InjectEntry(
    val id: String = Uuid.random().toString(),  // stable key for LazyColumn
    val date: LocalDate? = null,
    val hour: Int = 8,
    val minute: Int = 0,
    val amountText: String = "",                // raw text input; validated on insert
)

sealed interface HistoryInjectDevState {
    data object Loading : HistoryInjectDevState
    data class Data(
        val entries: List<InjectEntry> = listOf(InjectEntry()),
        val clearByDay: LocalDate? = null,
        val insertResult: String = "",          // feedback message after insert
    ) : HistoryInjectDevState
}
```

### ViewModel actions

```kotlin
fun addEntry()                               // appends a blank InjectEntry
fun removeEntry(id: String)                  // removes one row
fun updateEntry(id: String, entry: InjectEntry)
fun insertAll()                              // validate + insert all entries to DB
fun clearAll()                               // DELETE all HistoryEntity rows
fun setClearByDay(date: LocalDate)
fun clearByDay()                             // DELETE all rows for clearByDay date
```

**`insertAll()` logic:**
```kotlin
entries.forEach { entry ->
    val amountMl = entry.amountText.trim().toFloatOrNull() ?: return@forEach (skip invalid)
    if (amountMl <= 0f) return@forEach
    val date = entry.date ?: return@forEach (skip entries without a date)
    val timestamp = LocalDateTime(date, LocalTime(entry.hour, entry.minute))
    historyDao.insertAll(HistoryEntity(amount = amountMl, status = Status.IN_APP, timestamp = timestamp))
}
```

**`clearByDay()` logic** (add a new DAO query):
```kotlin
// New query in HistoryDao:
@Query("DELETE FROM HistoryEntity WHERE date(timestamp) = :date")
suspend fun deleteAllForDate(date: LocalDate)
```

Actually since Room stores `LocalDateTime`, not a SQL `date`, the range approach is safer:
```kotlin
// Use existing findByTimestamp to find records, then delete each one.
// Or add a new query:
@Query("DELETE FROM HistoryEntity WHERE timestamp BETWEEN :start AND :end")
suspend fun deleteAllBetween(start: LocalDateTime, end: LocalDateTime)
```
ViewModel calls: `deleteAllBetween(date.atStartOfDay(), date.atEndOfDay())`

### Screen layout

```
[ Bulk Editor section ]
  Header: "Inject Records"  +  [+] button (adds a row)
  LazyColumn of InjectEntry rows:
    Each row:
      DateTimePickerButton (date)   AppTimePickerButton (hh:mm)   AmountInputField   [✕]
  [Insert All] primary button

[ Clear section ]
  HorizontalDivider + "Danger Zone" header
  [Clear All Records] danger button (full width, confirm dialog)
  Spacer
  "Clear by day:"
  DateTimePickerButton (day selection)
  [Clear Selected Day] danger button (only enabled when a day is selected)
```

### Wiring into DevRoot

1. Add `HISTORY_INJECT("History", DwIcons.History)` to `DevDestination` enum.
2. Add `DevDestination.HISTORY_INJECT -> HistoryInjectDevRoot(onBackClicked)` to the `when` block.
3. Register `viewModelOf(::HistoryInjectDevViewModel)` in `FeatureModule.kt`.

### Reuse existing components

| Component | Source |
|-----------|--------|
| `DateTimePickerButton` | `ui/historylist/DatePicker.kt` |
| `AppTimePickerButton` | already imported in `DevRoot.kt` |
| `AmountInputField` | `ui/inputfield/AmountInputField.kt` |
| `DwPrimaryButton` / `DwSecondaryButton` | design system |

---

## Task 4 — Stats graph: previous days not showing

### Analysis

`StatsViewModel` initializes:
```kotlin
private val _chartPage = MutableStateFlow(0)
```

Pages are built oldest-first (index 0 = oldest week, index N = current week). On first
composition the chart shows `pages[0]` — the **oldest** week — which has no data if the app
was installed recently and only today's drinks exist.

`onPeriodSelected()` resets `_chartPage = Int.MAX_VALUE` (clamps to `pages.size - 1`, the
newest week), but this is only called when the user **taps** a period tab. On initial load
no tab tap occurs, so the chart never jumps to the current week.

Secondary concern: the query range starts from `firstGoalDate`'s Monday:
```kotlin
historyDao.findByTimestamp(firstMonday.atStartOfDay(), today.atEndOfDay())
```
If the user has drink records from before the first goal entry, those records are outside the
query range and will never appear.

### Fix 1 — Default to newest page

Change the initial value:
```kotlin
private val _chartPage = MutableStateFlow(Int.MAX_VALUE)
```
`safeIndex = page.coerceIn(0, (pages.size - 1).coerceAtLeast(0))` already handles the clamp,
so this single change makes the chart show the current week on first load.

### Fix 2 — Extend query range to include pre-goal records

Change the query start to use the earliest available drink record instead of the earliest goal:

```kotlin
// In loadWeekPages(), loadMonthPages(), loadYearPages():
// Replace: firstGoalDate.minus(...)
// With: use the minimum of (firstGoalDate, firstDrinkDate) as the range start.
```

Use `historyDao.getEarliestRecord()` (already exists in `HistoryDao`) to get the earliest drink.
Combine both flows and use `minOf(firstGoalMonday, firstDrinkMonday)` as the true start.

Or simpler — just use the goal-driven start as-is for the pages, but extend the
`findByTimestamp` query back to the first drink record, so records before the first goal
still appear in the totals when they fall within a rendered week page.

---

## Task 5 — Progress ring: hold-to-compress, release-to-bounce

### Problem

`bounceTap` in `RingAnimations.kt` uses `.clickable`, which fires the compress + bounce sequence
atomically the moment a tap is detected. The user cannot "feel" the hold state — pressing and
holding yields no visual feedback; the ring springs back before the finger is lifted.

### Desired behaviour

- **Press** → ring immediately compresses to 93 % scale.
- **Hold** → ring stays at 93 % for as long as the finger is down.
- **Release** → ring springs back with the bouncy animation; `onClick()` fires.
- **Cancel** (e.g. finger dragged off) → ring snaps back quickly; `onClick()` does **not** fire.

### Fix — `RingAnimations.kt`

Replace `.clickable` + `RingAnimationEvent` / `LaunchedEffect` + `MutableSharedFlow` with
`pointerInput` + `detectTapGestures(onPress)`:

```kotlin
fun Modifier.bounceTap(
    onClick: () -> Unit,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    rippleEnabled: Boolean = false,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val currentEnabled = rememberUpdatedState(enabled)
    val currentOnClick = rememberUpdatedState(onClick)

    this
        .scale(scale.value)
        .clip(shape)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { _ ->
                    if (!currentEnabled.value) {
                        // still consume the press so the click fires via tryAwaitRelease
                        val released = tryAwaitRelease()
                        if (released) currentOnClick.value()
                        return@detectTapGestures
                    }
                    // Compress immediately on press
                    scale.animateTo(0.93f, spring(stiffness = Spring.StiffnessHigh))
                    // Suspend until finger lifts or gesture is cancelled
                    val released = tryAwaitRelease()
                    if (released) {
                        // Bouncy spring-back + fire callback
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        )
                        currentOnClick.value()
                    } else {
                        // Gesture cancelled — snap back without callback
                        scale.animateTo(1f, spring(stiffness = Spring.StiffnessHigh))
                    }
                },
            )
        }
}
```

**Remove entirely:**
- `RingAnimationEvent` class
- `bounceEvent` / `interactions` `MutableSharedFlow`
- `LaunchedEffect(bounceEvent)` block
- `MutableInteractionSource` (no longer needed)

**Imports to add:** `androidx.compose.foundation.gestures.detectTapGestures`,
`androidx.compose.ui.input.pointer.pointerInput`

**Imports to remove:** `androidx.compose.foundation.clickable`,
`androidx.compose.foundation.LocalIndication`,
`androidx.compose.foundation.interaction.MutableInteractionSource`,
`kotlinx.coroutines.channels.BufferOverflow`,
`kotlinx.coroutines.flow.MutableSharedFlow`,
`kotlinx.coroutines.launch`

> `RingAnimationEvent` is only instantiated inside `bounceTap`'s `composed` block, so removing
> it has no effect on other files.

---

## Commit plan

1. `fix: pass unitPreference to GoalBottomSheet and NotificationAmountBottomSheet in SettingsScreen`
2. `fix: unit-aware inline validation in goal and notification amount steppers`
3. `feat(dev): history inject screen — bulk insert, clear all, clear by day`
4. `fix(stats): default chart to newest page; include pre-goal drink records in query`
5. `fix(ui): hold-to-compress, release-to-bounce on progress ring tap`

---

## Verification

- **T1:** Change unit to fl oz → open Settings → daily goal row shows e.g. "67.6 fl oz". Tap row → sheet opens showing the same fl oz value on the stepper.
- **T2:** In goal sheet with fl oz, tap the value, type "67" → no error. Type "0" → error. Type "20000" → error (exceeds fl oz max ~3381). On Save, the DB stores the mL equivalent.
- **T2 (L):** Change to Auto(L). Goal is 2000mL → displayed "2.0 L". Tap → type "2.5" → valid, saves 2500mL. Type "200" → error (exceeds L max ~100).
- **T3:** Open dev menu → History tab. Add 3 rows with different dates. Tap Insert All → records appear in the app's History screen on those dates. Tap Clear Selected Day for one date → records for that day only are removed. Tap Clear All → full DB cleared.
- **T4:** Open Stats screen → chart immediately shows the current week (not an older empty week). Previous weeks' pages (if any) show correct bar heights for logged drinks.
- **T5:** Press and hold the progress ring → it stays compressed at ~93 % while held. Release → bouncy spring-back plays and a drink is logged. Drag finger off during hold → ring snaps back, no drink logged.
