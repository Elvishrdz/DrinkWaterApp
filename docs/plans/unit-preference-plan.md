# Unit Preference Feature Plan

## Context

The app currently displays all volume amounts hardcoded in milliliters (mL). Users in different regions prefer different units: most of the world uses mL/L, while the US primarily uses fluid ounces (fl oz). This feature adds a `UnitPreference` setting that controls how volume amounts are displayed throughout the entire app, following the same architecture already established by `TimeFormatPreference`.

Storage always remains in mL. The preference only affects display/formatting.

---

## New Enum

**Create:** `compose/src/main/java/com/eahm/drinkwaterapp/core/presentation/UnitPreference.kt`

```kotlin
enum class UnitPreference {
    ML,           // Always show milliliters: "250 mL"
    AUTO_LITERS,  // Auto-convert above 1000: "250 mL" / "1.5 L"
    FL_OZ,        // US fluid ounces: "8.5 fl oz"
}
```

---

## New Utility Function

**Add to:** `compose/src/main/java/com/eahm/drinkwaterapp/core/domain/extension/Float.kt`

```kotlin
fun Float.formatVolume(pref: UnitPreference): String = when (pref) {
    UnitPreference.ML -> "${sanitizeZeroDecimals()} mL"
    UnitPreference.AUTO_LITERS ->
        if (this >= 1000f) "${"%.1f".format(this / 1000f)} L"
        else "${sanitizeZeroDecimals()} mL"
    UnitPreference.FL_OZ -> "${"%.1f".format(this / 29.574f)} fl oz"
}
```

`sanitizeZeroDecimals()` already exists in the same file.

---

## DataStore Layer

**Modify:** `PrefsStorage.kt`
- Add: `fun getUnitPreference(): Flow<UnitPreference>`
- Add: `suspend fun setUnitPreference(pref: UnitPreference)`

**Modify:** `DataStorePrefsStorage.kt`
- Add `stringPreferencesKey("UNIT_PREFERENCE")`
- Implement get: `runCatching { UnitPreference.valueOf(it) }.getOrNull() ?: UnitPreference.ML`
- Implement set: stores `pref.name`
- Follow exact same pattern as `timeFormatPreferenceKey`

---

## String Resources

**Modify:** `res/values/strings.xml`
- `settings_group_app` — "App"
- `settings_unit_preference_title` — "Volume Units"
- `settings_unit_ml` — "Milliliters (mL)"
- `settings_unit_auto_liters` — "Auto Liters (mL / L)"
- `settings_unit_fl_oz` — "Fluid Ounces (fl oz)"

---

## Settings Feature

### SettingsState.kt
Add field:
```kotlin
val unitPreference: UnitPreference = UnitPreference.ML,
```

### SettingsViewModel.kt
- Chain after existing `getTimeFormatPreference()` combine:
```kotlin
.combine(prefsStorage.getUnitPreference()) { state, unitPref ->
    state.copy(unitPreference = unitPref)
}
```
- Add: `fun onUnitPreferenceChanged(pref: UnitPreference) { viewModelScope.launch { prefsStorage.setUnitPreference(pref) } }`

### SettingsScreen.kt
1. Rename the existing **"Preferences" group** (currently holds TimeFormat row) to **"App"** group
2. Add a new `Units` row below TimeFormat inside the "App" group
3. Add `UnitPreferenceBadge(preference: UnitPreference)` composable — same pill style as `AlarmTypeBadge` / `TimeFormatBadge`
4. Add `var showUnitSheet by remember { mutableStateOf(false) }` local state
5. Add `UnitPreferenceBottomSheet` (3 radio options: ML / AUTO_LITERS / FL_OZ)

### GoalBottomSheet.kt
- Pass `unitPreference: UnitPreference` param
- Display the current mL value formatted: `currentGoal.formatVolume(unitPreference)` (large number display)
- The label below changes to reflect the current unit ("Milliliters" / "Liters" / "Fl oz")
- Step/min/max constants stay in mL — no logic changes

### NotificationAmountBottomSheet.kt
- Pass `unitPreference: UnitPreference` param
- Display current mL value formatted: `currentAmount.formatVolume(unitPreference)`
- Label below changes per unit
- Step/min/max constants stay in mL — no logic changes

---

## Drink Feature

### DrinkState.kt — `Data` class
Add field:
```kotlin
val unitPreference: UnitPreference = UnitPreference.ML,
```

### DrinkViewModel.kt
Chain after existing state combine:
```kotlin
.combine(prefsStorage.getUnitPreference()) { drinkState, unitPref ->
    if (drinkState is DrinkState.Data) drinkState.copy(unitPreference = unitPref)
    else drinkState
}
```

### Display sites (receive `unitPreference` from state):

| Component | What changes |
|-----------|-------------|
| **Progress ring** (DrinkScreen / ring composable) | current amount label + goal label use `formatVolume` |
| **WaterChip.kt** | `amount.formatVolume(unitPreference)` replaces `amount.toDigitString() + " mL"` |
| **AddDrinkButton.kt** | Input field hint: add `"mL"` text to placeholder (input is always in mL) |
| **TodayLogSection / DrinkLogEntry** | `entry.amount.formatVolume(unitPreference)` |

---

## History Feature

### HistoryState.kt — `Data` class
Add field:
```kotlin
val unitPreference: UnitPreference = UnitPreference.ML,
```

### HistoryViewModel.kt
Chain after existing `.combine(prefsStorage.getTimeFormatPreference())`:
```kotlin
.combine(prefsStorage.getUnitPreference()) { historyState, unitPref ->
    historyState.copy(unitPreference = unitPref)
}
```

### HistoryScreen.kt
- Pass `unitPreference` down to `Content` → `DaySummaryRow` → `StatChip`
- `StatChip` value: `consumedMl.formatVolume(unitPreference)` / `goalMl.formatVolume(unitPreference)`

### HistoryDialog.kt / HistoryTimeline
- `TimelineItemCard`: item amount display: `item.amount.toFloat().formatVolume(unitPreference)`
  (note: `HistoryUI.amount` is `String`, convert to Float first)

---

## Stats Feature

### StatsState.kt
Add field:
```kotlin
val unitPreference: UnitPreference = UnitPreference.ML,
```

### StatsViewModel.kt
Chain: `.combine(prefsStorage.getUnitPreference()) { state, unitPref -> state.copy(unitPreference = unitPref) }`

### Display sites:

| Component | What changes |
|-----------|-------------|
| **SummaryCard.kt** | `avgMl.formatVolume(unitPreference)` replaces `avgMl.toString()` + `mlDayUnit` label |
| **WeeklyBarChart.kt** | Tooltip: `amountMl.formatVolume(unitPreference)` and `goalMl.formatVolume(unitPreference)` |
| **MiniStatsCards.kt** | Personal best: `personalBestMl.formatVolume(unitPreference)` replaces `sanitizeZeroDecimals()` |

---

## Goal Celebration Overlay

### Problem
`GoalCelebrationCard` is displayed from a bound Service, not a Compose ViewModel. The unit preference must be passed via the `Intent` extra.

### Solution
1. In `DrinkViewModel.displayGoalReachedOverlay()`: read `unitPreference` from current state, pass it as a String extra in the Intent:
```kotlin
val unitPref = (state.value as? DrinkState.Data)?.unitPreference ?: UnitPreference.ML
// pass unitPref.name as intent extra
```
2. In `GoalCelebrationOverlay` / `GoalCelebrationCard.kt`: receive the unit pref string, parse it, apply `goalMl.formatVolume(unitPref)` to replace the hardcoded `"X.X L"` display.

---

## Implementation Order

1. `UnitPreference.kt` — create enum
2. `Float.kt` — add `formatVolume()` extension
3. `PrefsStorage.kt` + `DataStorePrefsStorage.kt` — DataStore layer
4. `strings.xml` — add all new string resources
5. `SettingsState.kt` + `SettingsViewModel.kt` — settings state
6. `SettingsScreen.kt` — UI group, badge, bottom sheet
7. `GoalBottomSheet.kt` + `NotificationAmountBottomSheet.kt` — settings steppers
8. `DrinkState.kt` + `DrinkViewModel.kt` — drink state
9. Drink display sites (ring, WaterChip, AddDrinkButton, TodayLogSection)
10. `HistoryState.kt` + `HistoryViewModel.kt` — history state
11. History display sites (HistoryScreen, HistoryTimeline)
12. `StatsState.kt` + `StatsViewModel.kt` — stats state
13. Stats display sites (SummaryCard, WeeklyBarChart, MiniStatsCards)
14. Goal celebration overlay (DrinkViewModel intent extra + GoalCelebrationCard)

---

## Critical Files

| File | Change type |
|------|-------------|
| `core/presentation/UnitPreference.kt` | **NEW** |
| `core/domain/extension/Float.kt` | Add `formatVolume()` |
| `core/data/datastore/PrefsStorage.kt` | Add 2 methods |
| `core/data/datastore/DataStorePrefsStorage.kt` | Implement 2 methods |
| `res/values/strings.xml` | Add ~5 strings |
| `feature/settings/SettingsState.kt` | Add field |
| `feature/settings/SettingsViewModel.kt` | Chain combine + add setter |
| `feature/settings/SettingsScreen.kt` | New group, badge, bottom sheet |
| `feature/settings/ui/GoalBottomSheet.kt` | Display unit |
| `feature/settings/ui/NotificationAmountBottomSheet.kt` | Display unit |
| `feature/drink/DrinkState.kt` | Add field |
| `feature/drink/DrinkViewModel.kt` | Chain combine + overlay fix |
| Drink display sites (ring, WaterChip, AddDrinkButton, TodayLogSection) | Display unit |
| `feature/history/HistoryState.kt` | Add field |
| `feature/history/HistoryViewModel.kt` | Chain combine |
| `feature/history/HistoryScreen.kt` | Pass down + display |
| `ui/historylist/HistoryDialog.kt` | TimelineItemCard display |
| `feature/stats/StatsState.kt` | Add field |
| `feature/stats/StatsViewModel.kt` | Chain combine |
| Stats display sites (SummaryCard, WeeklyBarChart, MiniStatsCards) | Display unit |
| `GoalCelebrationCard.kt` / overlay | Receive + apply unit pref |

---

## Reused Patterns

- `sanitizeZeroDecimals()` — `core/domain/extension/Float.kt` (reused inside `formatVolume`)
- `TimeFormatPreference` DataStore pattern — exact same approach for storage/retrieval
- `AlarmTypeBadge` / `TimeFormatBadge` — same pill style for `UnitPreferenceBadge`
- `.combine(prefsStorage.get...())` chain pattern — used in all ViewModels

---

## Verification

1. Change unit preference in Settings → verify all screens update immediately (reactive flow)
2. **mL**: all values show integers with "mL" suffix
3. **AUTO_LITERS**: values < 1000 show "X mL", values ≥ 1000 show "X.X L" (e.g., daily goal 2000 → "2.0 L")
4. **fl oz**: all values converted (e.g., 250 mL → "8.5 fl oz")
5. Notification amount stepper: increment/decrement still moves by 50 mL steps regardless of unit
6. Daily goal stepper: same — steps in mL, displays in current unit
7. Goal celebration overlay: shows goal in selected unit
8. Settings rows (daily goal, notification amount) reflect current unit in their display value
9. History summary (consumed/goal) + log items show in selected unit
10. Stats average, bar chart tooltip, personal best in selected unit

---

## Notes

- Also create a copy of this plan at: `docs/plans/unit-preference-plan.md`
- `HistoryUI.amount` is a `String` — convert to `Float` before calling `formatVolume`
- The `AddDrinkButton` input always collects in mL; only the hint text gains "mL" label
- No changes to DAO or database schema — all storage stays in mL
