# Alarm Notification Feature — Next Alarm Calculation

## Context

`DrinkAlarmManager.setupAlarm()` is supposed to determine when to fire the next drink reminder using
`DrinkAlarmCalculator2`. The current code is broken in two ways:

1. It takes `schedule.slots.first().time` (which always equals `now`, since `startTime = now`) and
   treats those `hour`/`minute` values as a millisecond duration — producing a wildly incorrect delay
   (e.g. at 14:30 it schedules 14 h 30 m from now instead of the actual interval).
2. It reads `drinkStats.remainingAmount`, a property that does not exist on `DrinkStats`.

Additionally, `startTime` from prefs is never read — only `endTime` is fetched.

The goal of this feature is to introduce a **pure, fully-testable `NextAlarmCalculator`** that owns
the "given the current time, when is the next alarm?" logic, wire it into `DrinkAlarmManager`, and
cover every case with unit tests.

---

## Cases

Given: `now`, `startTime`, `endTime`, `remainingMl`, `amountPerDrinkMl`.

| # | Condition | Result |
|---|-----------|--------|
| 1 | `remainingMl ≤ 0` (goal reached) | **GoalReached** — cancel alarm, do not reschedule |
| 2 | `now < startTime` (before window) | **BeforeWindow** — schedule delay until window opens |
| 3 | `now ≥ endTime` (at or after window end) | **AfterWindow** — schedule delay until tomorrow's `startTime` |
| 4 | `startTime ≤ now < endTime`, count > 1 | **Schedule** — next drink is `slots[1]`; delay = `slots[1].time − now` |
| 5 | `startTime ≤ now < endTime`, count == 1 | **LastDrink** — done for today; delay until tomorrow's `startTime` |

### Edge cases
- `now == startTime` for **LastDrink** → delay = 24 h exactly (avoids zero from `calculateTimeDifference`)
- Overnight windows (`endTime < startTime`, e.g. 22:00–06:00) — `DayTime.calculateTimeDifference`
  already handles wrap-around; window membership check must also account for this
- `startTime == endTime` (zero-length window) → treated as **AfterWindow**
- `amountPerDrink > remaining` → count rounds to 1 → **LastDrink**

---

## Data Flow

```
AlarmReceiver fires
    │
    ├─ showDrinkNotification()
    │
    └─ scheduleNextAlarm()
           │
           └─ DrinkAlarmManager.setupAlarm(context)
                  │
                  ├─ read now (Clock.System)
                  ├─ read startTime, endTime from PrefsStorage
                  ├─ read amountPerDrink from PrefsStorage
                  ├─ read drinkStats from DrinkStatsObserver
                  │      remaining = goalAmount - drinkAmount
                  │
                  └─ NextAlarmCalculator.calculate(now, start, end, remaining, perDrink)
                         │
                         ├─ GoalReached    → skip (alarm already cancelled)
                         ├─ BeforeWindow   → AlarmScheduler.scheduleAlarm(delayMillis)
                         ├─ AfterWindow    → AlarmScheduler.scheduleAlarm(delayMillis)
                         ├─ Schedule       → AlarmScheduler.scheduleAlarm(delayMillis)
                         └─ LastDrink      → AlarmScheduler.scheduleAlarm(delayMillis)
```

---

## New Components

### `NextAlarmResult` (sealed class)
`core/data/drinkalarm/NextAlarmResult.kt`

```kotlin
sealed class NextAlarmResult {
    /** Daily goal already reached — cancel alarm, do not reschedule. */
    object GoalReached : NextAlarmResult()

    /** Before the active window opens. Delay until window start. */
    data class BeforeWindow(val delayMillis: Long) : NextAlarmResult()

    /** Active window has closed for today. Delay until tomorrow's window start. */
    data class AfterWindow(val delayMillis: Long) : NextAlarmResult()

    /** Within window; next drink at [nextSlot]. */
    data class Schedule(val delayMillis: Long, val nextSlot: DrinkSlot) : NextAlarmResult()

    /** Last scheduled drink is happening now. Done for today; delay until tomorrow. */
    data class LastDrink(val delayMillis: Long) : NextAlarmResult()
}
```

### `NextAlarmCalculator` (pure class)
`core/data/drinkalarm/NextAlarmCalculator.kt`

No Android imports. Fully testable with JVM unit tests.

**Constructor:**
```kotlin
class NextAlarmCalculator(
    private val calculator: DrinkAlarmCalculator2 = DrinkAlarmCalculator2()
)
```

**Public API:**
```kotlin
fun calculate(
    now: DayTime,
    startTime: DayTime,
    endTime: DayTime,
    remainingMl: Float,
    amountPerDrinkMl: Float,
): NextAlarmResult
```

**Internal logic:**
1. `remainingMl ≤ 0f` → `GoalReached`
2. Determine window membership (handle normal and overnight windows)
3. Before window → `BeforeWindow(delayMillis)`
4. After/at window end → `AfterWindow(delayMillis)`
5. Within window:
   - `calculator.scheduleByAmount(startTime = now, endTime, remainingMl, amountPerDrinkMl)`
   - `count == 1` → `LastDrink(tomorrowDelayMillis(now, startTime))`
   - `count > 1` → `Schedule(minutesDiff(now, slots[1].time) * 60_000L, slots[1])`

**Private helpers:**
```kotlin
// DayTime.calculateTimeDifference handles overnight wrap-around
private fun minutesDiff(from: DayTime, to: DayTime): Long =
    DayTime.calculateTimeDifference(from, to).inWholeMinutes.toLong()

// Delay until tomorrow's startTime; treats diff==0 as 24 h
private fun tomorrowDelayMillis(now: DayTime, startTime: DayTime): Long {
    val diffMinutes = minutesDiff(now, startTime)
    val adjusted = if (diffMinutes == 0L) 24 * 60L else diffMinutes
    return adjusted * 60_000L
}
```

---

## Modified Files

### `DrinkAlarmManager.kt`
`core/data/drinkalarm/DrinkAlarmManager.kt`

- Add `private val nextAlarmCalculator = NextAlarmCalculator()` (no DI needed — pure class)
- Fix `setupAlarm()`:
  - Read `startTime` from `prefsStorage.getStartTime()` (currently missing)
  - Fix remaining: `val remaining = stats.goalAmount - stats.drinkAmount`
  - Replace broken scheduler call with `nextAlarmCalculator.calculate(...)` → `when` dispatch
- Remove/clean dead commented-out code

### `DrinkStats` (optional improvement)
Add computed property to `DrinkStats` in `DrinkStatsObserver.kt`:
```kotlin
val remainingAmount: Float get() = goalAmount - drinkAmount
```

---

## Tests

### `NextAlarmCalculatorTest.kt`
`compose/src/test/java/com/eahm/drinkwaterapp/core/data/drinkalarm/NextAlarmCalculatorTest.kt`

Reference window: `start = 07:00`, `end = 22:00` (15 h = 900 min).

#### Before Window
| ID  | now   | Expected       | Delay           |
|-----|-------|----------------|-----------------|
| BW-1 | 06:00 | BeforeWindow | 60 min = 3 600 000 ms |
| BW-2 | 06:59 | BeforeWindow | 1 min = 60 000 ms |
| BW-3 | 00:00 | BeforeWindow | 420 min = 25 200 000 ms |

#### After Window
| ID  | now   | Expected     | Delay (to next 07:00)       |
|-----|-------|--------------|-----------------------------|
| AW-1 | 22:00 | AfterWindow | 540 min = 32 400 000 ms |
| AW-2 | 22:01 | AfterWindow | 539 min |
| AW-3 | 23:59 | AfterWindow | 421 min |

#### Goal Reached
| ID  | remaining | Expected    |
|-----|-----------|-------------|
| GR-1 | 0.0      | GoalReached |
| GR-2 | -50.0    | GoalReached |

#### Within Window — Schedule (multiple drinks)
| ID  | now   | remaining | perDrink | Expected count | Constraint        |
|-----|-------|-----------|----------|----------------|-------------------|
| WW-1 | 07:00 | 2000 | 250 | 8 | delayMs > 0 |
| WW-2 | 14:00 | 1000 | 250 | 4 | delayMs > 0 |
| WW-3 | 07:01 | 2000 | 250 | ≥ 2 | result is Schedule |
| WW-4 | any   | —    | —   | —  | nextSlot.time > now |

#### Within Window — Last Drink (count rounds to 1)
| ID  | now   | remaining | perDrink | Expected     |
|-----|-------|-----------|----------|--------------|
| LD-1 | 21:00 | 50       | 500      | LastDrink    |
| LD-2 | 20:00 | 100      | 500      | LastDrink    |
| LD-3 | any   | —        | —        | delay > 0    |

#### Delay Correctness
| ID  | Description |
|-----|-------------|
| DC-1 | BW-1 delay exactly 3 600 000 ms |
| DC-2 | AW-1 delay exactly 32 400 000 ms |
| DC-3 | WW-1 Schedule.delayMillis in `(0, 15h_in_ms]` |

#### Edge Cases
| ID  | Description | Expected |
|-----|-------------|----------|
| EG-1 | now == startTime, count > 1 | Schedule (not BeforeWindow) |
| EG-2 | now == startTime, count rounds to 1 | LastDrink, delay == 24 h |
| EG-3 | startTime == endTime | AfterWindow |
| EG-4 | Overnight window (end < start), now inside window | Schedule or LastDrink |
| EG-5 | Overnight window, now outside window | BeforeWindow or AfterWindow |

---

## Verification

```bash
# Run new tests only
./gradlew :compose:test --tests "*.NextAlarmCalculatorTest"

# Regression — existing calculator tests still pass
./gradlew :compose:test --tests "*.DrinkAlarmCalculator2Test"

# All unit tests
./gradlew :compose:test
```

**Manual device verification:**
1. Enable alarms; confirm notification fires approximately at the correct time (within-window case)
2. Tap "Drink" → next notification reschedules correctly (count > 1 path)
3. Wait until past `endTime` → next notification appears at next day's `startTime`
4. Reach daily goal mid-day → no further notifications fire until next day
