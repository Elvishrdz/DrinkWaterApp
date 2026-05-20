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

// Convert back to mL on valid save:
val amountMl = when (displayUnit) {
    DisplayUnit.ML    -> parsed
    DisplayUnit.FL_OZ -> parsed * 29.574f
    DisplayUnit.L     -> parsed * 1000f
}
onValueSet(amountMl)
```

The `OutlinedTextField` label (hint) should also reflect the display unit so the user knows
what to type. Change `settings_stepper_custom_hint` to be unit-aware, or add a suffix label:

```kotlin
label = { Text(stringResource(R.string.settings_stepper_custom_hint) + " (${displayUnit.label})") }
```

Add `val label: String` to `DisplayUnit`:
```kotlin
enum class DisplayUnit(val label: String) { ML("mL"), L("L"), FL_OZ("fl oz") }
```

> **Note:** The `effectiveDisplayUnit` helper and `DisplayUnit` enum can be placed in a shared
> `UnitConversionUtils.kt` inside `core/domain/extension/` since all four files need it.

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

## Commit plan

1. `fix: pass unitPreference to GoalBottomSheet and NotificationAmountBottomSheet in SettingsScreen`
2. `fix: unit-aware inline validation in goal and notification amount steppers`
3. `feat(dev): history inject screen — bulk insert, clear all, clear by day`
4. `fix(stats): default chart to newest page; include pre-goal drink records in query`

---

## Verification

- **T1:** Change unit to fl oz → open Settings → daily goal row shows e.g. "67.6 fl oz". Tap row → sheet opens showing the same fl oz value on the stepper.
- **T2:** In goal sheet with fl oz, tap the value, type "67" → no error. Type "0" → error. Type "20000" → error (exceeds fl oz max ~3381). On Save, the DB stores the mL equivalent.
- **T2 (L):** Change to Auto(L). Goal is 2000mL → displayed "2.0 L". Tap → type "2.5" → valid, saves 2500mL. Type "200" → error (exceeds L max ~100).
- **T3:** Open dev menu → History tab. Add 3 rows with different dates. Tap Insert All → records appear in the app's History screen on those dates. Tap Clear Selected Day for one date → records for that day only are removed. Tap Clear All → full DB cleared.
- **T4:** Open Stats screen → chart immediately shows the current week (not an older empty week). Previous weeks' pages (if any) show correct bar heights for logged drinks.
